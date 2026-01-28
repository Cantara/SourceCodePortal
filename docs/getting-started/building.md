# Building Source Code Portal

Comprehensive guide to building the application with Maven.

## Prerequisites

- **Java 21 LTS** - Required for compilation
- **Maven 3.6+** - Build tool
- **(Optional) Sass** - For compiling stylesheets

## Basic Build Commands

### Full Clean Build with Tests

```bash
mvn clean install
```

**What it does**:
- Removes all previous build artifacts (`target/` directory)
- Downloads dependencies (first run only)
- Compiles Java sources
- Runs all unit and integration tests
- Packages application as JAR
- Installs to local Maven repository (~/.m2/repository)

**Build time**:
- First run: ~3-5 minutes (downloads dependencies)
- Subsequent: ~1-2 minutes

**Output**: `target/source-code-portal-{version}.jar`

### Fast Build (Skip Tests)

```bash
mvn clean install -DskipTests
```

**When to use**:
- Quick iterations during development
- Building for local testing
- After making small changes

**Build time**: ~30-60 seconds

⚠️ **Warning**: Always run full build before committing or deploying.

### Build Without Install

```bash
mvn clean package
```

**What it does**:
- Same as `install` but doesn't copy JAR to local Maven repo
- Slightly faster if you don't need the JAR in your local repository

### Compile Only (No Packaging)

```bash
mvn clean compile
```

**When to use**:
- Just checking if code compiles
- Before running tests in IDE

## Testing Commands

### Run All Tests

```bash
mvn test
```

Runs all tests without recompiling if no changes detected.

### Run Single Test Class

```bash
mvn test -Dtest=TestClassName
```

**Example**:
```bash
mvn test -Dtest=SpringBootServerTest
mvn test -Dtest=CacheStoreTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=TestClassName#methodName
```

**Example**:
```bash
mvn test -Dtest=SpringBootServerTest#testHealthEndpoint
mvn test -Dtest=GitHubCommandTest#testRateLimit
```

### Run Tests Matching Pattern

```bash
mvn test -Dtest='*Controller*'
mvn test -Dtest='*Spring*'
```

### Skip Tests Entirely

```bash
mvn clean install -DskipTests       # Compile tests but don't run
mvn clean install -Dmaven.test.skip # Don't even compile tests (faster)
```

## Frontend Build Commands

### Compile Sass to CSS

Source Code Portal uses Sass for stylesheets. You have two options:

#### Option 1: Maven Plugin (Slower)

```bash
mvn com.github.warmuuh:libsass-maven-plugin:watch
```

**Pros**:
- No external dependencies
- Works on any system with Maven

**Cons**:
- Slower compilation
- More verbose output

#### Option 2: Native Sass (Recommended)

First, install Sass (one-time setup):
```bash
# macOS
brew install sass/sass/sass

# Ubuntu/Debian
npm install -g sass

# Windows
choco install sass
```

Then watch for changes:
```bash
sass --watch src/main/sass/scss:target/classes/META-INF/views/css
```

**Pros**:
- Much faster (~10x)
- Cleaner output
- Better error messages

**Cons**:
- Requires external installation

### Manual CSS Compilation

If you just want to compile once (no watching):

```bash
# With Maven
mvn com.github.warmuuh:libsass-maven-plugin:compile

# With Sass
sass src/main/sass/scss:target/classes/META-INF/views/css
```

### IntelliJ File Watcher

For automatic compilation on save in IntelliJ:

1. **Settings** → **Tools** → **File Watchers**
2. Click **+** → **Sass**
3. Configure:
   - **File type**: SCSS
   - **Program**: `/usr/local/bin/sass` (adjust path)
   - **Arguments**: `$FileName$:$FileNameWithoutExtension$.css`
   - **Output paths**: `$FileNameWithoutExtension$.css`
4. Click **OK**

Now CSS will compile automatically when you save SCSS files.

## Build Profiles

### Development Profile

```bash
mvn clean install -Pdev
```

Enables:
- Debug logging
- Hot reload (with Spring Boot DevTools)
- Detailed error pages

### Production Profile

```bash
mvn clean install -Pprod
```

Enables:
- Optimized compilation
- Minified resources
- Production logging levels

### Test Profile

```bash
mvn clean install -Ptest
```

Used for:
- Integration test environments
- CI/CD pipelines

## Advanced Build Options

### Parallel Builds

Speed up builds with multiple threads:

```bash
mvn clean install -T 4          # Use 4 threads
mvn clean install -T 1C         # Use 1 thread per CPU core
mvn clean install -T 2.0C       # Use 2 threads per CPU core
```

**Recommended**: `-T 1C` on modern multi-core systems

### Offline Mode

Build without checking for dependency updates:

```bash
mvn clean install -o
# or
mvn clean install --offline
```

**When to use**:
- No internet connection
- Speed up builds when dependencies are already cached

### Update Dependencies

Force update of SNAPSHOT dependencies:

```bash
mvn clean install -U
# or
mvn clean install --update-snapshots
```

### Verbose Output

For debugging build issues:

```bash
mvn clean install -X    # Debug output
mvn clean install -e    # Show error stack traces
```

## Build Artifacts

After a successful build, you'll find:

```
target/
├── source-code-portal-{version}.jar    # Executable JAR
├── classes/                             # Compiled Java classes
│   └── META-INF/
│       └── views/                       # Thymeleaf templates
│           └── css/                     # Compiled CSS
├── test-classes/                        # Compiled test classes
├── maven-archiver/                      # Maven metadata
└── maven-status/                        # Build status
```

### Executable JAR

The main artifact is a "fat JAR" containing all dependencies:

```bash
ls -lh target/source-code-portal-*.jar
# ~50-60 MB
```

You can run it directly:
```bash
java -jar target/source-code-portal-*.jar
```

## Build Troubleshooting

### "mvn: command not found"

Maven is not installed or not on PATH:

```bash
# Check installation
which mvn

# Install Maven
# macOS: brew install maven
# Ubuntu: apt install maven
# Windows: choco install maven
```

### "Could not find or load main class"

Clean build artifacts and rebuild:

```bash
mvn clean
rm -rf target/
mvn install -DskipTests
```

### Out of Memory

Increase Maven memory:

```bash
export MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
mvn clean install
```

Or set permanently in `~/.mavenrc`:
```bash
MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
```

### Dependency Download Failures

```bash
# Clear corrupted dependencies
rm -rf ~/.m2/repository/

# Retry with verbose output
mvn clean install -U -X
```

### Test Failures

Run specific failing test with verbose output:

```bash
mvn test -Dtest=FailingTest -X
```

See test reports:
```bash
cat target/surefire-reports/FailingTest.txt
```

### Sass Compilation Errors

Check Sass syntax:
```bash
sass --check src/main/sass/scss/main.scss
```

Common issues:
- **Missing semicolons**: Each rule must end with `;`
- **Invalid nesting**: Check bracket matching
- **Import errors**: Verify file paths in `@import`

### Maven Plugin Errors

Update plugin versions:

```bash
mvn versions:display-plugin-updates
```

Or force plugin update:
```bash
mvn clean install -U
```

## Build Performance Tips

1. **Use parallel builds**: `-T 1C` for multi-core systems
2. **Skip tests during development**: `-DskipTests`
3. **Use offline mode**: `-o` when dependencies are cached
4. **Increase Maven memory**: `MAVEN_OPTS="-Xmx2g"`
5. **Use native Sass**: Much faster than Maven plugin
6. **Use IntelliJ build**: Often faster than command-line Maven

## CI/CD Build

For continuous integration:

```bash
# Standard CI build command
mvn clean verify -B

# With code coverage
mvn clean verify -B jacoco:report

# With integration tests
mvn clean verify -B -Pintegration-tests
```

The `-B` flag enables batch mode (non-interactive, cleaner logs).

## Next Steps

- **[Running Guide](running.md)** - Learn how to run the built application
- **[Configuration Guide](configuration.md)** - Configure the application
- **[Development - Testing](../development/testing.md)** - Learn about the test framework

## See Also

- Maven official documentation: https://maven.apache.org/guides/
- Sass documentation: https://sass-lang.com/documentation/
- [LEARNINGS.md](../../LEARNINGS.md) - Build-related gotchas
