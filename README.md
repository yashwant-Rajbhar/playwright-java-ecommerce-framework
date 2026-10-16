# E-Commerce UI + API Automation Framework (Playwright + Java)

![CI](https://github.com/YOUR-USERNAME/YOUR-REPO/actions/workflows/ci.yml/badge.svg)

End-to-end test automation for [automationexercise.com](https://automationexercise.com), a public practice
e-commerce site. The framework covers login, product search, cart, checkout and payment, plus API tests and
API + UI combination tests.

## Tech stack

Java 17 · Playwright for Java · TestNG · Maven · Log4j2 · Gson · Allure · GitHub Actions

## What it demonstrates

- **Page Object Model.** Pages hold locators and actions, tests hold assertions.
- **Safe parallel execution.** One Playwright / Browser / Context / Page per thread (`ThreadLocal`), and a fresh
  isolated `BrowserContext` for every test.
- **Cross-browser.** Chromium, Firefox and WebKit, chosen with `-Dbrowser=...` or by a TestNG suite file.
- **Layered configuration.** Command line, then `config-<env>.properties`, then `config.properties`.
  Run another environment with `-Denv=stage`.
- **Failure evidence.** On a failed test: full-page screenshot, Playwright trace (DOM, network, actions) and video.
  Passing tests keep nothing.
- **Retry for flaky tests.** A failed test is retried once. Retried tests stay visible in the log as `SKIP`.
- **API layer.** Playwright's `APIRequestContext` tests the site's REST endpoints.
- **API + UI tests.** The API creates a unique user, the UI uses it, and the API deletes it. Every checkout
  test has its own account, so there is no shared state.
- **Test data in JSON** (`testdata.json`), not in Java.
- **Allure reports** and **CI** with GitHub Actions.

## Architecture

```
Test class (LoginTest, CartTest, ...)   what to verify
        |  uses
Page Objects (LoginPage, CartPage, ...) how to interact with one page
        |  uses
Playwright Page                         the browser
        ^  created by
BaseTest + PlaywrightFactory            browser, context, trace, video, cleanup
        ^  configured by
ConfigReader                            -D options, config-<env>.properties, config.properties
```

## Project structure

```
src/main/java
  api/         ApiClient (REST calls through Playwright)
  base/        BaseTest, PlaywrightFactory
  pages/       HomePage, LoginPage, ProductPage, ProductDetailsPage, CartPage, CheckoutPage,
               PaymentPage, OrderConfirmationPage
  listeners/   TestListener, RetryAnalyzer, RetryTransformer
  utils/       ConfigReader, TestDataReader, ScreenshotUtil, PriceUtil, PageLoad
src/main/resources
  config.properties, config-dev|qa|stage.properties, testdata.json, log4j2.xml
src/test/java
  tests/       LoginTest, ProductTest, CartTest, CheckoutTest, ApiUiTest
  api/         ProductApiTest
testng.xml                    parallel run (by class), the default suite
testng-crossbrowser.xml       Chromium + Firefox + WebKit at the same time
.github/workflows             CI on every push, nightly cross-browser run
```

## Getting started

Requirements: JDK 17+, Maven, Git.

1. Create a test user on the site (Signup / Login page).
2. Create `src/main/resources/config-local.properties` (it is git-ignored):
```properties
   validEmail=your-test-user@example.com
   validPassword=your-password
```
3. Install the browsers once:
```
   mvn compile exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

## Running tests

| Goal | Command |
|---|---|
| Default suite (parallel by class) | `mvn clean test` |
| Headless | `mvn clean test -Dheadless=true` |
| One class | `mvn clean test -Dtest=CartTest` |
| One test | `mvn clean test -Dtest=CartTest#removeProduct` |
| A browser | `mvn clean test -Dbrowser=firefox` |
| All three browsers | `mvn clean test -DsuiteFile=testng-crossbrowser.xml -Dheadless=true` |
| Another environment | `mvn clean test -Denv=stage` |

In Windows PowerShell, put `-D` options with a dot in quotes: `"-DsuiteFile=testng-crossbrowser.xml"`.

## Reports and evidence

- **Allure:** run `mvn allure:serve` after a test run.
- **Failures:** `reports/screenshots`, `reports/traces`, `reports/videos`. Open a trace by dragging the zip onto
  https://trace.playwright.dev.

## CI

- `ci.yml`: every push and pull request runs the suite on Chromium (headless), then uploads the Allure
  results and any failure evidence as build artifacts.
- `nightly-crossbrowser.yml`: every night (and on demand) runs the three-browser suite.
- The test account comes from the repository secrets `VALID_EMAIL` and `VALID_PASSWORD`.

## Notes and limitations

- The target is a public practice site. It can be slow, and it is slower still when three browsers run in
  parallel, so a few tests are occasionally flaky, mostly on WebKit. That is why the page objects wait for real
  content, reload once if a page arrives empty, and why failed tests are retried once.
- The site has no coupons, stock levels or sorting, so those scenarios from a typical e-commerce plan are not
  covered.