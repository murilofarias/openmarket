#!/bin/bash

################################################################################
# OpenMarket Metrics Report Generator
#
# Generates a consolidated metrics report from JMeter results
################################################################################

RESULTS_FILE="${1:-results.jtl}"
OUTPUT_FILE="${2:-metrics-report.txt}"
JSON_OUTPUT="${OUTPUT_FILE%.txt}.json"

# Colors for terminal output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BOLD}${BLUE}$1${NC}"
}

print_success() {
    echo -e "${GREEN}$1${NC}"
}

# Check if results file exists
if [ ! -f "$RESULTS_FILE" ]; then
    echo -e "${RED}[ERROR]${NC} Results file not found: $RESULTS_FILE"
    echo "Usage: $0 [results.jtl] [output-file.txt]"
    exit 1
fi

# Get timestamp for report
REPORT_DATE=$(date '+%Y-%m-%d %H:%M:%S')
TEST_DATE=$(head -2 "$RESULTS_FILE" | tail -1 | cut -d',' -f1)
if [ -n "$TEST_DATE" ]; then
    TEST_DATE_FORMATTED=$(date -d "@$((TEST_DATE/1000))" '+%Y-%m-%d %H:%M:%S' 2>/dev/null || echo "N/A")
else
    TEST_DATE_FORMATTED="N/A"
fi

# AWK script for parsing JTL CSV (handles quoted fields with commas)
read -r -d '' AWK_PARSE_CSV << 'AWKSCRIPT'
BEGIN {
    FS=","
    OFS="\t"
}
NR == 1 { next }  # Skip header
{
    # Handle quoted fields (failureMessage may contain commas)
    line = $0
    in_quotes = 0
    field = ""
    field_num = 1

    # Arrays to store parsed fields
    delete fields

    for (i = 1; i <= length(line); i++) {
        c = substr(line, i, 1)
        if (c == "\"") {
            in_quotes = !in_quotes
        } else if (c == "," && !in_quotes) {
            fields[field_num] = field
            field_num++
            field = ""
        } else {
            field = field c
        }
    }
    fields[field_num] = field  # Last field

    # Extract fields by position (JTL format)
    # 1:timeStamp, 2:elapsed, 3:label, 4:responseCode, 5:responseMessage,
    # 6:threadName, 7:dataType, 8:success, 9:failureMessage, 10:bytes,
    # 11:sentBytes, 12:grpThreads, 13:allThreads, 14:URL, 15:Latency,
    # 16:IdleTime, 17:Connect

    timestamp = fields[1]
    elapsed = fields[2]
    label = fields[3]
    responseCode = fields[4]
    success = fields[8]
    bytes = fields[10]
    sentBytes = fields[11]
    latency = fields[15]
    connect = fields[17]

    # Output parsed data
    print timestamp, elapsed, label, responseCode, success, bytes, sentBytes, latency, connect
}
AWKSCRIPT

# Parse JTL file into clean TSV format
PARSED_DATA=$(echo "$AWK_PARSE_CSV" | awk -f - "$RESULTS_FILE")

# Generate TXT report
{
    echo "================================================================================"
    echo "                    OPENMARKET INTEGRATION TEST METRICS REPORT"
    echo "================================================================================"
    echo ""
    echo "Report Generated: $REPORT_DATE"
    echo "Test Executed:    $TEST_DATE_FORMATTED"
    echo "Results File:     $RESULTS_FILE"
    echo ""
    echo "--------------------------------------------------------------------------------"
    echo "                              METRICS LEGEND"
    echo "--------------------------------------------------------------------------------"
    echo ""
    echo "  ELAPSED (ms)  : Total response time from request sent to response received."
    echo "                  This is the complete round-trip time for the HTTP request."
    echo ""
    echo "  LATENCY (ms)  : Time to First Byte (TTFB). Time from request sent until"
    echo "                  the first byte of response is received. Indicates server"
    echo "                  processing time + network latency."
    echo ""
    echo "  CONNECT (ms)  : TCP connection establishment time. Time to complete the"
    echo "                  TCP handshake with the server. High values may indicate"
    echo "                  network issues or server overload. Value is 0 when"
    echo "                  connection is reused (Keep-Alive)."
    echo ""
    echo "  BYTES         : Size of the response body in bytes."
    echo ""
    echo "  SENT BYTES    : Size of the request body in bytes."
    echo ""
    echo "================================================================================"
    echo "                              DETAILED METRICS"
    echo "================================================================================"
    echo ""
    printf "%-35s %10s %10s %10s %10s %10s %8s\n" \
        "ENDPOINT" "ELAPSED" "LATENCY" "CONNECT" "BYTES" "SENT" "STATUS"
    printf "%-35s %10s %10s %10s %10s %10s %8s\n" \
        "-----------------------------------" "----------" "----------" "----------" "----------" "----------" "--------"

    # Process parsed data for display
    echo "$PARSED_DATA" | while IFS=$'\t' read -r timestamp elapsed label responseCode success bytes sentBytes latency connect; do
        # Determine status icon
        if [ "$success" = "true" ]; then
            status="✓ OK"
        else
            status="✗ FAIL"
        fi

        # Truncate label if too long
        if [ ${#label} -gt 33 ]; then
            label="${label:0:30}..."
        fi

        printf "%-35s %10s %10s %10s %10s %10s %8s\n" \
            "$label" "${elapsed}ms" "${latency}ms" "${connect}ms" "$bytes" "$sentBytes" "$status"
    done

    echo ""
    echo "================================================================================"
    echo "                              SUMMARY STATISTICS"
    echo "================================================================================"
    echo ""

    # Calculate summary statistics
    echo "$PARSED_DATA" | awk -F'\t' '
    BEGIN {
        total_elapsed = 0
        total_latency = 0
        total_connect = 0
        total_bytes = 0
        total_sent = 0
        min_elapsed = 999999
        max_elapsed = 0
        count = 0
        errors = 0
    }
    {
        elapsed = $2
        success = $5
        bytes = $6
        sent = $7
        latency = $8
        connect = $9

        total_elapsed += elapsed
        total_latency += latency
        total_connect += connect
        total_bytes += bytes
        total_sent += sent

        if (elapsed < min_elapsed) min_elapsed = elapsed
        if (elapsed > max_elapsed) max_elapsed = elapsed

        if (success != "true") errors++
        count++
    }
    END {
        if (count > 0) {
            avg_elapsed = total_elapsed / count
            avg_latency = total_latency / count
            avg_connect = total_connect / count

            printf "  Total Requests:     %d\n", count
            printf "  Successful:         %d\n", count - errors
            printf "  Failed:             %d\n", errors
            printf "  Error Rate:         %.2f%%\n", (errors / count) * 100
            printf "\n"
            printf "  Response Time (Elapsed):\n"
            printf "    - Average:        %.2f ms\n", avg_elapsed
            printf "    - Min:            %d ms\n", min_elapsed
            printf "    - Max:            %d ms\n", max_elapsed
            printf "\n"
            printf "  Latency (TTFB):\n"
            printf "    - Average:        %.2f ms\n", avg_latency
            printf "\n"
            printf "  Connection Time:\n"
            printf "    - Average:        %.2f ms\n", avg_connect
            printf "\n"
            printf "  Data Transfer:\n"
            printf "    - Total Received: %.2f KB\n", total_bytes / 1024
            printf "    - Total Sent:     %.2f KB\n", total_sent / 1024
        }
    }'

    echo ""
    echo "================================================================================"
    echo "                           PERFORMANCE INSIGHTS"
    echo "================================================================================"
    echo ""

    # Find slowest endpoints
    echo "  Top 3 Slowest Endpoints:"
    echo "$PARSED_DATA" | sort -t$'\t' -k2 -nr | head -3 | while IFS=$'\t' read -r timestamp elapsed label rest; do
        printf "    - %-40s %s ms\n" "$label" "$elapsed"
    done

    echo ""

    # Find fastest endpoints
    echo "  Top 3 Fastest Endpoints:"
    echo "$PARSED_DATA" | sort -t$'\t' -k2 -n | head -3 | while IFS=$'\t' read -r timestamp elapsed label rest; do
        printf "    - %-40s %s ms\n" "$label" "$elapsed"
    done

    echo ""
    echo "================================================================================"

} | tee "$OUTPUT_FILE"

echo ""
print_success "[INFO] Report saved to: $OUTPUT_FILE"

# Generate JSON report using awk for proper formatting
{
    echo "{"
    echo "  \"reportDate\": \"$REPORT_DATE\","
    echo "  \"testDate\": \"$TEST_DATE_FORMATTED\","
    echo "  \"resultsFile\": \"$RESULTS_FILE\","
    echo "  \"metrics\": ["

    # Use awk to generate JSON array with proper comma handling
    echo "$PARSED_DATA" | awk -F'\t' '
    BEGIN { first = 1 }
    {
        timestamp = $1
        elapsed = $2
        label = $3
        responseCode = $4
        success = $5
        bytes = $6
        sentBytes = $7
        latency = $8
        connect = $9

        # Escape quotes in label
        gsub(/"/, "\\\"", label)

        if (first) {
            first = 0
        } else {
            printf ",\n"
        }

        printf "    {\n"
        printf "      \"label\": \"%s\",\n", label
        printf "      \"elapsed\": %s,\n", elapsed
        printf "      \"latency\": %s,\n", latency
        printf "      \"connect\": %s,\n", connect
        printf "      \"bytes\": %s,\n", bytes
        printf "      \"sentBytes\": %s,\n", sentBytes
        printf "      \"responseCode\": \"%s\",\n", responseCode
        printf "      \"success\": %s\n", success
        printf "    }"
    }
    END { printf "\n" }
    '

    echo "  ],"

    # Add summary (LC_NUMERIC=C ensures decimal point instead of comma)
    echo "$PARSED_DATA" | LC_NUMERIC=C awk -F'\t' '
    BEGIN {
        total_elapsed = 0
        min_elapsed = 999999
        max_elapsed = 0
        count = 0
        errors = 0
    }
    {
        elapsed = $2
        success = $5
        total_elapsed += elapsed
        if (elapsed < min_elapsed) min_elapsed = elapsed
        if (elapsed > max_elapsed) max_elapsed = elapsed
        if (success != "true") errors++
        count++
    }
    END {
        if (count > 0) {
            printf "  \"summary\": {\n"
            printf "    \"totalRequests\": %d,\n", count
            printf "    \"successfulRequests\": %d,\n", count - errors
            printf "    \"failedRequests\": %d,\n", errors
            printf "    \"errorRate\": %.2f,\n", (errors / count) * 100
            printf "    \"avgResponseTime\": %.2f,\n", total_elapsed / count
            printf "    \"minResponseTime\": %d,\n", min_elapsed
            printf "    \"maxResponseTime\": %d\n", max_elapsed
            printf "  }\n"
        }
    }'

    echo "}"
} > "$JSON_OUTPUT"

# Validate JSON
if command -v python3 &> /dev/null; then
    if python3 -m json.tool "$JSON_OUTPUT" > /dev/null 2>&1; then
        print_success "[INFO] JSON report saved to: $JSON_OUTPUT (valid)"
    else
        echo -e "${YELLOW}[WARN]${NC} JSON report may have formatting issues: $JSON_OUTPUT"
    fi
else
    print_success "[INFO] JSON report saved to: $JSON_OUTPUT"
fi
