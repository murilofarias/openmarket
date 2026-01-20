# OpenMarket Integration Tests

Complete integration test suite for OpenMarket API using Apache JMeter.

## Test Scenario

This test simulates a complete user journey through the OpenMarket platform:

### Seller Flow
1. **Register Seller User** - Create a new seller account
2. **Login as Seller** - Authenticate and receive access token
3. **Create Seller Profile** - Set up store information
4. **Create Product** - Add a product to the store
5. **Update Product Stock** - Modify product inventory (10 → 50 units)

### Buyer Flow
6. **Register Buyer User** - Create a new buyer account
7. **Login as Buyer** - Authenticate and receive access token
8. **Create Buyer Profile** - Set up shipping information
9. **Search Products** - Find products by keyword and category (public endpoint)
10. **Get Product Details** - View complete product info including seller details (public endpoint)

## Prerequisites

### 1. Install Apache JMeter

**On Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install jmeter
```

**On macOS (using Homebrew):**
```bash
brew install jmeter
```

**On Windows:**
Download from [Apache JMeter Downloads](https://jmeter.apache.org/download_jmeter.cgi) and extract.

**Verify Installation:**
```bash
jmeter --version
```

### 2. Start Your OpenMarket Application

Ensure your Spring Boot application is running:
```bash
cd /home/murilo/NewProjects/openmarket
./mvnw spring-boot:run
```

Default URL: `http://localhost:8080`

### 3. Ensure Keycloak is Running

Make sure your Keycloak instance is configured and running as per your `application.yml` configuration.

## Running the Tests

### GUI Mode (Recommended for Development/Debugging)

1. **Open JMeter GUI:**
   ```bash
   jmeter
   ```

2. **Load the test plan:**
   - File → Open → Select `OpenMarket-Integration-Test.jmx`

3. **Configure base URL (if different from localhost:8080):**
   - Click on "OpenMarket - Complete Integration Test" in the tree
   - Modify `BASE_URL` variable in User Defined Variables

4. **Run the test:**
   - Click the green "Start" button (▶️) or press `Ctrl+R`

5. **View results:**
   - Click on "View Results Tree" to see detailed request/response
   - Click on "Summary Report" to see aggregated statistics

### Command Line Mode (Recommended for CI/CD)

Run the test from command line:

```bash
cd /home/murilo/NewProjects/openmarket/integration-tests

jmeter -n -t OpenMarket-Integration-Test.jmx \
       -l results.jtl \
       -e -o ./report
```

**Parameters:**
- `-n` : Non-GUI mode
- `-t` : Test file to run
- `-l` : Log file to save results
- `-e` : Generate report dashboard
- `-o` : Output folder for HTML report

**View HTML Report:**
```bash
# Open the generated report in your browser
xdg-open ./report/index.html  # Linux
open ./report/index.html       # macOS
start ./report/index.html      # Windows
```

### Run with Custom Base URL

```bash
jmeter -n -t OpenMarket-Integration-Test.jmx \
       -JBASE_URL=http://dev-server:8080 \
       -l results.jtl
```

## Test Assertions

The test includes the following validations:

### HTTP Status Codes
- **201 Created** - User registration, profile creation, product creation
- **200 OK** - Login, search, get product details
- **204 No Content** - Product update

### Response Data
- ✅ Seller user ID extracted correctly
- ✅ Seller access token received and valid
- ✅ Seller profile ID created
- ✅ Product ID created and accessible
- ✅ Product stock updated to 50
- ✅ Buyer user ID extracted correctly
- ✅ Buyer access token received and valid
- ✅ Buyer profile ID created
- ✅ Product found in search results
- ✅ Product details contain seller information (store name)

## Test Data

The test uses dynamic data with timestamps to avoid conflicts:

- **Seller Email:** `seller{timestamp}@test.com`
- **Buyer Email:** `buyer{timestamp}@test.com`
- **Password:** `Test@123456`
- **Store Name:** `Test Store {timestamp}`
- **Product Name:** `Test Product {timestamp}`

## Troubleshooting

### Test Fails at Registration Step

**Problem:** `401 Unauthorized` or connection refused

**Solution:**
1. Verify Keycloak is running
2. Check Keycloak configuration in `application.yml`
3. Ensure the OpenMarket API is running on the correct port

### Test Fails at "Create Seller Profile"

**Problem:** Seller profile creation fails even after successful registration

**Solution:**
1. Check if seller auto-approval is enabled, or
2. Manually approve the seller in your database:
   ```sql
   UPDATE seller_profiles
   SET status = 'ACTIVE'
   WHERE user_id = '[seller-user-id]';
   ```
3. You may need to add a delay or manual approval step

### Product Not Found in Search

**Problem:** Search returns empty results

**Solution:**
1. Verify product was created successfully (check PRODUCT_ID variable)
2. Ensure product status is ACTIVE:
   ```sql
   SELECT * FROM products WHERE id = '[product-id]';
   ```
3. Check if product needs to be published (if you have that workflow)

### Database Already Has Test Data

**Problem:** Email already exists error

**Solution:**
The test uses timestamps to generate unique emails. If this still happens:
1. Clean test data from database between runs:
   ```sql
   DELETE FROM products WHERE name LIKE 'Test Product%';
   DELETE FROM seller_profiles WHERE store_name LIKE 'Test Store%';
   DELETE FROM buyer_profiles WHERE id IN (
     SELECT id FROM users WHERE email LIKE '%@test.com'
   );
   -- Add more cleanup as needed
   ```
2. Or use a fresh database for testing

## Extending the Tests

### Add Order Creation

To extend the test to include order placement, add after step 2.5:

```xml
<HTTPSamplerProxy testname="2.6 Create Order">
  <stringProp name="HTTPSampler.path">/orders</stringProp>
  <stringProp name="HTTPSampler.method">POST</stringProp>
  <!-- Request body with product and quantity -->
</HTTPSamplerProxy>
```

### Add Review Creation

To test the review system, add after order creation:

```xml
<HTTPSamplerProxy testname="2.7 Create Review">
  <stringProp name="HTTPSampler.path">/products/${PRODUCT_ID}/reviews</stringProp>
  <stringProp name="HTTPSampler.method">POST</stringProp>
  <!-- Request body with rating and comment -->
</HTTPSamplerProxy>
```

### Load Testing

To perform load testing, modify the Thread Group:

1. Open the test in JMeter GUI
2. Click on "OpenMarket User Journey" thread group
3. Modify:
   - **Number of Threads:** 10 (simulate 10 concurrent users)
   - **Ramp-Up Period:** 10 seconds (start all users within 10 seconds)
   - **Loop Count:** 5 (each user repeats the journey 5 times)

## CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/integration-test.yml`:

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  integration-test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: openmarket_test
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Start Keycloak (or mock auth)
        run: |
          # Start Keycloak in Docker
          docker run -d -p 8081:8080 \
            -e KEYCLOAK_ADMIN=admin \
            -e KEYCLOAK_ADMIN_PASSWORD=admin \
            quay.io/keycloak/keycloak:latest start-dev

      - name: Build and Start Application
        run: |
          ./mvnw clean package -DskipTests
          java -jar target/*.jar &
          sleep 30  # Wait for app to start

      - name: Install JMeter
        run: |
          wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
          tar -xzf apache-jmeter-5.6.3.tgz
          export PATH=$PATH:$(pwd)/apache-jmeter-5.6.3/bin

      - name: Run Integration Tests
        run: |
          jmeter -n -t integration-tests/OpenMarket-Integration-Test.jmx \
                 -l results.jtl \
                 -e -o ./report

      - name: Upload Test Report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: jmeter-report
          path: ./report
```

## Results Interpretation

### View Results Tree
- **Green** samples = passed
- **Red** samples = failed
- Expand each request to see:
  - Request headers/body
  - Response headers/body
  - Assertions results

### Summary Report
- **Samples**: Number of requests executed
- **Average**: Average response time (ms)
- **Min/Max**: Fastest and slowest response times
- **Error %**: Percentage of failed requests
- **Throughput**: Requests per second

## Best Practices

1. **Clean Database Between Runs**: For consistent results, reset your test database
2. **Check Logs**: Monitor application logs during test execution
3. **Gradual Load**: Start with 1 user, then increase for load testing
4. **Use Variables**: Parameterize test data using JMeter variables
5. **Assertions**: Always verify response data, not just status codes

## Support

For issues or questions:
1. Check application logs: `./logs/spring-boot-application.log`
2. Check JMeter logs: `jmeter.log` (in JMeter installation directory)
3. Enable debug logging in JMeter: Add `-Jlog_level.jmeter=DEBUG` to command line

## License

This test suite is part of the OpenMarket project.
