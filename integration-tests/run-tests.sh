#!/bin/bash

################################################################################
# OpenMarket Integration Test Runner
################################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
BASE_URL="http://localhost:8080"
REPORT_DIR="./report"
RESULTS_FILE="results.jtl"
GUI_MODE=false

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if JMeter is installed
check_jmeter() {
    if ! command -v jmeter &> /dev/null; then
        print_error "JMeter is not installed or not in PATH"
        echo ""
        echo "Please install JMeter:"
        echo "  Ubuntu/Debian: sudo apt install jmeter"
        echo "  macOS: brew install jmeter"
        echo "  Or download from: https://jmeter.apache.org/download_jmeter.cgi"
        exit 1
    fi

    JMETER_VERSION=$(jmeter --version 2>&1 | head -n 1)
    print_info "JMeter found: $JMETER_VERSION"

    # Check if version is 3.0 or higher (required for -e flag)
    if jmeter --version 2>&1 | grep -q "Copyright (c) 1998-2015"; then
        print_warning "Your JMeter version is too old (pre-3.0)"
        print_warning "HTML report generation (-e flag) is not supported"
        print_warning "Please upgrade to JMeter 5.6+ for full functionality"
        echo ""
        echo "To upgrade JMeter:"
        echo "  1. Download latest version: https://jmeter.apache.org/download_jmeter.cgi"
        echo "  2. Extract: tar -xzf apache-jmeter-*.tgz"
        echo "  3. Add to PATH: export PATH=\$PATH:/path/to/apache-jmeter/bin"
        echo ""
        read -p "Do you want to continue with limited functionality? (y/N) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
        LEGACY_JMETER=true
    else
        LEGACY_JMETER=false
    fi
}

# Function to check if application is running
check_application() {
    print_info "Checking if OpenMarket API is running at $BASE_URL..."

    if curl -s -f -o /dev/null "$BASE_URL/api/actuator/health" 2>/dev/null; then
        print_info "Application is running!"
    elif curl -s -f -o /dev/null "$BASE_URL" 2>/dev/null; then
        print_info "Application is running (health endpoint not available)"
    else
        print_warning "Cannot reach application at $BASE_URL"
        print_warning "Make sure your Spring Boot application is running"
        echo ""
        read -p "Do you want to continue anyway? (y/N) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# Function to clean old results
clean_results() {
    if [ -d "$REPORT_DIR" ]; then
        print_info "Cleaning old results..."
        rm -rf "$REPORT_DIR"
    fi

    if [ -f "$RESULTS_FILE" ]; then
        rm -f "$RESULTS_FILE"
    fi
}

# Function to run tests in GUI mode
run_gui_mode() {
    print_info "Starting JMeter in GUI mode..."
    jmeter -t OpenMarket-Integration-Test.jmx
}

# Function to run tests in CLI mode
run_cli_mode() {
    print_info "Running integration tests in non-GUI mode..."
    print_info "Base URL: $BASE_URL"

    if [ "$LEGACY_JMETER" = true ]; then
        # Legacy JMeter (pre-3.0) - no HTML report generation
        print_warning "Running without HTML report generation (JMeter 3.0+ required)"

        jmeter -n \
               -t OpenMarket-Integration-Test.jmx \
               -JBASE_URL="$BASE_URL" \
               -l "$RESULTS_FILE"

        if [ $? -eq 0 ]; then
            print_info "Tests completed successfully!"
            print_info "Results saved to: $RESULTS_FILE"
            echo ""
            print_info "To view results:"
            print_info "  1. Open JMeter in GUI mode: jmeter"
            print_info "  2. Load test file: OpenMarket-Integration-Test.jmx"
            print_info "  3. Click 'Browse...' on a listener (e.g., View Results Tree)"
            print_info "  4. Select file: $RESULTS_FILE"
        else
            print_error "Tests failed! Check $RESULTS_FILE for details"
            exit 1
        fi
    else
        # Modern JMeter (3.0+) - with HTML report generation
        jmeter -n \
               -t OpenMarket-Integration-Test.jmx \
               -JBASE_URL="$BASE_URL" \
               -l "$RESULTS_FILE" \
               -e \
               -o "$REPORT_DIR"

        if [ $? -eq 0 ]; then
            print_info "Tests completed successfully!"
            print_info "Results saved to: $RESULTS_FILE"
            print_info "HTML Report generated at: $REPORT_DIR/index.html"
            echo ""

            # Generate consolidated metrics report
            SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
            if [ -f "$SCRIPT_DIR/generate-metrics-report.sh" ]; then
                echo ""
                "$SCRIPT_DIR/generate-metrics-report.sh" "$RESULTS_FILE" "metrics-report.txt"
            fi

            echo ""
            print_info "Opening HTML report in browser..."

            # Open report in browser (cross-platform)
            if command -v xdg-open &> /dev/null; then
                xdg-open "$REPORT_DIR/index.html"
            elif command -v open &> /dev/null; then
                open "$REPORT_DIR/index.html"
            elif command -v start &> /dev/null; then
                start "$REPORT_DIR/index.html"
            else
                echo "Please open $REPORT_DIR/index.html manually in your browser"
            fi
        else
            print_error "Tests failed! Check $RESULTS_FILE for details"
            exit 1
        fi
    fi
}

# Function to display usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Run OpenMarket integration tests using Apache JMeter.

OPTIONS:
    -g, --gui           Run in GUI mode (for debugging)
    -u, --url URL       Base URL of the API (default: http://localhost:8080)
    -c, --clean         Clean old test results before running
    -h, --help          Display this help message

EXAMPLES:
    # Run tests against localhost
    $0

    # Run tests in GUI mode
    $0 --gui

    # Run tests against a different server
    $0 --url http://dev-server:8080

    # Clean old results and run tests
    $0 --clean

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -g|--gui)
            GUI_MODE=true
            shift
            ;;
        -u|--url)
            BASE_URL="$2"
            shift 2
            ;;
        -c|--clean)
            clean_results
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Main execution
print_info "OpenMarket Integration Test Runner"
echo ""

# Check prerequisites
check_jmeter

# Check if test file exists
if [ ! -f "OpenMarket-Integration-Test.jmx" ]; then
    print_error "Test file 'OpenMarket-Integration-Test.jmx' not found!"
    print_error "Please run this script from the integration-tests directory"
    exit 1
fi

# Check if application is running (skip in GUI mode)
if [ "$GUI_MODE" = false ]; then
    check_application
fi

# Always clean old results before running
clean_results

# Run tests
echo ""
if [ "$GUI_MODE" = true ]; then
    run_gui_mode
else
    run_cli_mode
fi

print_info "Done!"
