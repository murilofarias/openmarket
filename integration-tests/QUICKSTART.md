# Quick Start Guide - Integration Tests

Get up and running with OpenMarket integration tests in 5 minutes!

## Prerequisites Checklist

- [ ] Java 21 installed
- [ ] PostgreSQL running
- [ ] Keycloak configured and running
- [ ] OpenMarket application running
- [ ] Apache JMeter installed

## Step-by-Step Setup

### 1. Install JMeter (if not installed)

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install jmeter
```

**macOS:**
```bash
brew install jmeter
```

**Verify:**
```bash
jmeter --version
```

### 2. Start Your Application

```bash
cd /home/murilo/NewProjects/openmarket
./mvnw spring-boot:run
```

Wait until you see: `Started OpenmarketApplication in X seconds`

### 3. Run the Integration Tests

**Option A: Using the Shell Script (Recommended)**
```bash
cd integration-tests
./run-tests.sh
```

The script will:
- ✅ Check if JMeter is installed
- ✅ Check if your application is running
- ✅ Run all tests
- ✅ Generate HTML report
- ✅ Open report in browser

**Option B: Run Manually**
```bash
cd integration-tests

# Non-GUI mode (for CI/CD)
jmeter -n -t OpenMarket-Integration-Test.jmx \
       -l results.jtl \
       -e -o ./report

# View report
xdg-open ./report/index.html  # Linux
```

**Option C: GUI Mode (for debugging)**
```bash
cd integration-tests
./run-tests.sh --gui
```

Or:
```bash
jmeter -t OpenMarket-Integration-Test.jmx
```

## Understanding Test Results

### ✅ All Tests Passed
You should see:
- 10 HTTP requests executed
- 0% error rate
- All assertions passed (green checkmarks)

### ❌ Tests Failed

**Common Issues:**

1. **Connection Refused**
   - ➡️ Solution: Make sure your Spring Boot app is running on port 8080

2. **401 Unauthorized on Registration**
   - ➡️ Solution: Check Keycloak is running and configured correctly

3. **Seller Profile Creation Fails**
   - ➡️ Solution: Check if seller auto-approval is enabled, or manually approve:
   ```sql
   UPDATE seller_profiles SET status = 'ACTIVE'
   WHERE user_id = 'xxx';
   ```

4. **Product Not Found in Search**
   - ➡️ Solution: Check if product status is ACTIVE and published

## Test Flow Overview

```
SELLER JOURNEY
├── 1.1 Register Seller User ✓
├── 1.2 Login as Seller ✓
├── 1.3 Create Seller Profile ✓
├── 1.4 Create Product ✓
└── 1.5 Update Product Stock ✓

BUYER JOURNEY
├── 2.1 Register Buyer User ✓
├── 2.2 Login as Buyer ✓
├── 2.3 Create Buyer Profile ✓
├── 2.4 Search Products (public) ✓
└── 2.5 Get Product Details (public) ✓
```

## Cleaning Test Data

After running tests, clean up test data:

**Option 1: Using SQL Script**
```bash
psql -U your_user -d openmarket -f cleanup-test-data.sql
```

**Option 2: Manually**
```sql
-- Connect to your database
psql -U your_user -d openmarket

-- Run cleanup
DELETE FROM products WHERE name LIKE 'Test Product%';
DELETE FROM seller_profiles WHERE store_name LIKE 'Test Store%';
DELETE FROM users WHERE email LIKE '%@test.com';
```

## Customizing Tests

### Change Base URL

```bash
./run-tests.sh --url http://your-server:8080
```

### Run Against Different Environment

Edit the test file or use JMeter properties:

```bash
jmeter -n -t OpenMarket-Integration-Test.jmx \
       -JBASE_URL=http://staging.example.com \
       -l results.jtl
```

## Viewing Detailed Results

### In GUI Mode
1. Click "View Results Tree" listener
2. Expand each request to see:
   - Request body/headers
   - Response body/headers
   - Assertion results

### In CLI Mode
Open the generated HTML report:
```bash
xdg-open ./report/index.html  # Linux
open ./report/index.html       # macOS
```

Key metrics:
- **Samples**: Number of requests (should be 10)
- **Error %**: Should be 0.00%
- **Average**: Average response time
- **Throughput**: Requests per second

## Next Steps

1. **Add More Tests**: Extend the test to include order creation and reviews
2. **Load Testing**: Increase thread count to simulate multiple users
3. **CI/CD Integration**: Add to your GitHub Actions workflow
4. **Scheduled Tests**: Run tests automatically every night

## Need Help?

Check the main [README.md](./README.md) for:
- Detailed troubleshooting guide
- Advanced configuration options
- CI/CD integration examples
- Load testing instructions

## Pro Tips

💡 **Run tests before committing**: Catch bugs early
```bash
./run-tests.sh && git commit -m "Feature complete"
```

💡 **Use clean database for tests**: Consistent results
```bash
psql -f cleanup-test-data.sql && ./run-tests.sh
```

💡 **Monitor logs during tests**: See what's happening
```bash
tail -f logs/spring-boot-application.log
```

💡 **Debug failed tests in GUI mode**: Visual debugging
```bash
./run-tests.sh --gui
```

---

**Happy Testing! 🚀**
