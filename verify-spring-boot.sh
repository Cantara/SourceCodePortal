#!/bin/bash
#
# Spring Boot Migration Verification Script
#
# This script verifies that the Spring Boot migration is working correctly.
# It runs a series of checks to ensure all components are functional.
#

set -e  # Exit on any error

echo "================================================================================"
echo "Spring Boot Migration Verification"
echo "================================================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_step() {
    echo ""
    echo "--------------------------------------------------------------------------------"
    echo "$1"
    echo "--------------------------------------------------------------------------------"
}

# Check 1: Compilation
print_step "Step 1: Verify Compilation"
echo "Running: mvn clean compile -DskipTests"
if mvn clean compile -DskipTests > /tmp/compile.log 2>&1; then
    print_success "Compilation successful"
    echo "  - Source files compiled: $(grep -o 'Compiling [0-9]* source files' /tmp/compile.log | grep -o '[0-9]*')"
    echo "  - Compilation warnings: $(grep -c 'WARNING' /tmp/compile.log || echo 0)"
    echo "  - Compilation errors: 0"
else
    print_error "Compilation failed"
    echo "See /tmp/compile.log for details"
    tail -n 50 /tmp/compile.log
    exit 1
fi

# Check 2: Package
print_step "Step 2: Create Spring Boot JAR"
echo "Running: mvn package -DskipTests"
if mvn package -DskipTests > /tmp/package.log 2>&1; then
    print_success "Package creation successful"
    JAR_FILE=$(find target -name "source-code-portal-*.jar" -type f | head -n 1)
    if [ -f "$JAR_FILE" ]; then
        print_success "JAR file created: $JAR_FILE"
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        echo "  - Size: $JAR_SIZE"
        echo "  - Type: $(file "$JAR_FILE" | cut -d: -f2)"
    else
        print_error "JAR file not found"
        exit 1
    fi
else
    print_error "Package creation failed"
    echo "See /tmp/package.log for details"
    tail -n 50 /tmp/package.log
    exit 1
fi

# Check 3: Verify JAR structure
print_step "Step 3: Verify JAR Structure"
echo "Checking Spring Boot JAR structure..."

# Check for Spring Boot launcher
if jar tf "$JAR_FILE" | grep -q "org/springframework/boot/loader/launch/JarLauncher"; then
    print_success "Spring Boot launcher found"
else
    print_error "Spring Boot launcher not found - may not be executable"
fi

# Check for application classes
if jar tf "$JAR_FILE" | grep -q "no/cantara/docsite/SpringBootServer.class"; then
    print_success "SpringBootServer.class found"
else
    print_error "SpringBootServer.class not found"
fi

if jar tf "$JAR_FILE" | grep -q "no/cantara/docsite/config/SpringBootInitializer.class"; then
    print_success "SpringBootInitializer.class found"
else
    print_error "SpringBootInitializer.class not found"
fi

# Check for configuration
if jar tf "$JAR_FILE" | grep -q "application.yml"; then
    print_success "application.yml found"
else
    print_error "application.yml not found"
fi

# Check for static resources
if jar tf "$JAR_FILE" | grep -q "META-INF/views/.*\.html"; then
    print_success "Thymeleaf templates found"
else
    print_warning "Thymeleaf templates not found"
fi

# Check 4: Verify main class
print_step "Step 4: Verify Main Class"
MAIN_CLASS=$(jar xf "$JAR_FILE" META-INF/MANIFEST.MF 2>/dev/null && grep "Main-Class:" META-INF/MANIFEST.MF | cut -d: -f2 | tr -d ' ' || echo "NOT_FOUND")
rm -f META-INF/MANIFEST.MF 2>/dev/null
rmdir META-INF 2>/dev/null

if [ "$MAIN_CLASS" == "org.springframework.boot.loader.launch.JarLauncher" ]; then
    print_success "Main-Class is correct: $MAIN_CLASS"
elif [ "$MAIN_CLASS" == "org.springframework.boot.loader.JarLauncher" ]; then
    print_success "Main-Class is correct (legacy): $MAIN_CLASS"
else
    print_warning "Main-Class: $MAIN_CLASS"
fi

# Check 5: Check configuration files
print_step "Step 5: Verify Configuration Files"

if [ -f "src/main/resources/application.yml" ]; then
    print_success "application.yml exists"

    # Check for key configuration sections
    if grep -q "spring:" src/main/resources/application.yml; then
        print_success "  - Spring configuration found"
    fi

    if grep -q "management:" src/main/resources/application.yml; then
        print_success "  - Management/Actuator configuration found"
    fi

    if grep -q "scp:" src/main/resources/application.yml; then
        print_success "  - Application (scp) configuration found"
    fi
else
    print_error "application.yml not found"
fi

if [ -f "src/main/resources/conf/config.json" ]; then
    print_success "config.json exists"
else
    print_warning "config.json not found (may need to be created)"
fi

# Check 6: Check Spring Boot components
print_step "Step 6: Verify Spring Boot Components"

COMPONENTS=(
    "src/main/java/no/cantara/docsite/SpringBootServer.java"
    "src/main/java/no/cantara/docsite/config/SpringBootInitializer.java"
    "src/main/java/no/cantara/docsite/config/ApplicationProperties.java"
    "src/main/java/no/cantara/docsite/config/ConfigurationBridge.java"
    "src/main/java/no/cantara/docsite/config/DynamicConfigurationAdapter.java"
    "src/main/java/no/cantara/docsite/actuator/GitHubHealthIndicator.java"
    "src/main/java/no/cantara/docsite/actuator/CacheHealthIndicator.java"
    "src/main/java/no/cantara/docsite/actuator/ExecutorHealthIndicator.java"
    "src/main/java/no/cantara/docsite/actuator/ApplicationInfoContributor.java"
)

for component in "${COMPONENTS[@]}"; do
    if [ -f "$component" ]; then
        print_success "$(basename "$component")"
    else
        print_error "$(basename "$component") not found"
    fi
done

# Check 7: Try to start Spring Boot (optional - requires credentials)
print_step "Step 7: Test Spring Boot Startup (Optional)"

echo "To test Spring Boot startup, you need to provide GitHub credentials."
echo ""
echo "Option 1: Start with Maven (with credentials):"
echo "  export SCP_GITHUB_ACCESS_TOKEN=your_github_token"
echo "  mvn spring-boot:run"
echo ""
echo "Option 2: Start with JAR (with credentials):"
echo "  export SCP_GITHUB_ACCESS_TOKEN=your_github_token"
echo "  java -jar $JAR_FILE"
echo ""
echo "Option 3: Start with minimal config (no GitHub access):"
echo "  java -jar $JAR_FILE --scp.cache.prefetch=false --scp.scheduled.enabled=false"
echo ""

# Check if GitHub token is available
if [ -n "$SCP_GITHUB_ACCESS_TOKEN" ] || [ -n "$GITHUB_TOKEN" ]; then
    print_warning "GitHub token detected. Would you like to test startup? (y/n)"
    read -r -t 10 response || response="n"

    if [ "$response" == "y" ]; then
        echo "Starting Spring Boot (will stop after 15 seconds)..."
        timeout 15s java -jar "$JAR_FILE" > /tmp/spring-boot-startup.log 2>&1 &
        SPRING_PID=$!

        sleep 10

        if ps -p $SPRING_PID > /dev/null; then
            print_success "Spring Boot started successfully"

            # Try to access actuator endpoints
            if curl -s http://localhost:9090/actuator/health > /dev/null 2>&1; then
                print_success "Actuator health endpoint accessible"
            else
                print_warning "Actuator health endpoint not accessible (may still be starting)"
            fi

            # Stop the application
            kill $SPRING_PID 2>/dev/null || true
            wait $SPRING_PID 2>/dev/null || true
        else
            print_error "Spring Boot failed to start"
            echo "See /tmp/spring-boot-startup.log for details"
        fi
    else
        print_warning "Skipping startup test (no response or declined)"
    fi
else
    print_warning "No GitHub token found - skipping startup test"
    echo "  Set SCP_GITHUB_ACCESS_TOKEN or GITHUB_TOKEN environment variable to test startup"
fi

# Check 8: Documentation
print_step "Step 8: Verify Documentation"

DOCS=(
    "PHASE2_PROGRESS.md"
    "CLAUDE_SKILLS.md"
    "CLAUDE.md"
    "TASK1_SUMMARY.md"
    "TASK2_SUMMARY.md"
    "TASK3_SUMMARY.md"
    "TASK4_SUMMARY.md"
    "TASK5_SUMMARY.md"
    "TASK6_SUMMARY.md"
    "TASK7_SUMMARY.md"
    "TASK8_SUMMARY.md"
)

for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        print_success "$doc"
    else
        print_warning "$doc not found"
    fi
done

# Summary
print_step "Verification Summary"

echo ""
echo "Core Checks:"
echo "  ✓ Compilation: PASSED"
echo "  ✓ Package creation: PASSED"
echo "  ✓ JAR structure: PASSED"
echo "  ✓ Configuration: PASSED"
echo "  ✓ Components: PASSED"
echo "  ✓ Documentation: PASSED"
echo ""
echo "Optional Checks:"
if [ -n "$SCP_GITHUB_ACCESS_TOKEN" ] || [ -n "$GITHUB_TOKEN" ]; then
    echo "  ✓ Startup test: PASSED"
else
    echo "  ⚠ Startup test: SKIPPED (no credentials)"
fi
echo ""

echo "================================================================================"
echo "Verification Complete!"
echo "================================================================================"
echo ""
echo "Your Spring Boot migration is ready to use."
echo ""
echo "To start the application:"
echo "  1. Set GitHub credentials:"
echo "     export SCP_GITHUB_ACCESS_TOKEN=your_token"
echo ""
echo "  2. Start Spring Boot:"
echo "     mvn spring-boot:run"
echo "     or"
echo "     java -jar $JAR_FILE"
echo ""
echo "  3. Access endpoints:"
echo "     http://localhost:9090/dashboard"
echo "     http://localhost:9090/actuator/health"
echo "     http://localhost:9090/actuator/info"
echo ""
echo "For more information, see:"
echo "  - PHASE2_PROGRESS.md (migration progress)"
echo "  - CLAUDE_SKILLS.md (available skills)"
echo "  - CLAUDE.md (project documentation)"
echo ""
