# Gotchas and Learnings - Source Code Portal Modernization

**Last Updated**: 2026-01-27
**Applies to**: Phase 1 (Java 21, Resilience4j, JUnit 5)

This document captures critical gotchas, lessons learned, and best practices discovered during modernization.

---

## 🚨 Critical Gotchas

### 1. Commonmark Group ID Change
**Symptom**: `Could not find artifact com.atlassian.commonmark:commonmark:jar:0.22.0`

**Problem**: Commonmark moved from Atlassian to its own organization.

**Fix**:
```xml
<!-- Wrong -->
<groupId>com.atlassian.commonmark</groupId>
<artifactId>commonmark</artifactId>
<version>0.22.0</version>

<!-- Correct -->
<groupId>org.commonmark</groupId>
<artifactId>commonmark</artifactId>
<version>0.22.0</version>
```

**Applies to**: All commonmark artifacts (commonmark, commonmark-ext-heading-anchor, commonmark-ext-gfm-tables)

---

### 2. Selenium 4 API Changes
**Symptom**: `cannot find symbol: method findElementById(String)`

**Problem**: Selenium 4 removed convenience methods like `findElementById()`.

**Fix**:
```java
// Wrong (Selenium 3)
driver.findElementById("login_field");
driver.findElementByName("commit");
driver.findElementByClassName("btn");

// Correct (Selenium 4)
driver.findElement(By.id("login_field"));
driver.findElement(By.name("commit"));
driver.findElement(By.className("btn"));
```

**Pattern**: All `findElementBy*()` methods → `findElement(By.*())`

**Migration Strategy**:
```bash
# Global find/replace (careful!)
find src -name "*.java" -exec sed -i \
  's/\.findElementById(\([^)]*\))/.findElement(By.id(\1))/g' {} +
```

---

### 3. JSoup normalise() Removal
**Symptom**: `cannot find symbol: method normalise()`

**Problem**: JSoup 1.18+ removed `normalise()` method (now automatic).

**Fix**:
```java
// Wrong (JSoup 1.13)
Document doc = Jsoup.parse(html);
Element body = doc.normalise().body();

// Correct (JSoup 1.18)
Document doc = Jsoup.parse(html);
Element body = doc.body();  // normalise() happens automatically
```

**Why**: JSoup now normalizes structure during parsing, making explicit call redundant.

---

### 4. TestNG → JUnit 5 Assertion Parameter Order
**Symptom**: Tests pass but assert wrong things

**Problem**: **TestNG and JUnit 5 have REVERSED parameter order!**

**Fix**:
```java
// TestNG (actual, expected)
Assert.assertEquals(actualValue, expectedValue);

// JUnit 5 (expected, actual) - REVERSED!
Assertions.assertEquals(expectedValue, actualValue);
```

**Critical**: This is a **logic error**, not a compilation error. Tests will compile but may assert incorrectly!

**Migration Strategy**:
```bash
# Manual review required - cannot be automated safely
# Check each assertEquals call individually
grep -rn "assertEquals" src/test/java
```

---

### 5. Java 17+ Removed --illegal-access Flag
**Symptom**: `Ignoring option --illegal-access=deny; support was removed in 17.0`

**Problem**: Flag used in Java 11 to detect reflection issues, removed in Java 17.

**Fix**:
```xml
<!-- Wrong (works in Java 11, warns in 17+) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>--illegal-access=deny</argLine>
    </configuration>
</plugin>

<!-- Correct (Java 17+) -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-Xmx1500m</argLine>  <!-- Remove illegal-access flag -->
    </configuration>
</plugin>
```

---

### 6. Hystrix → Resilience4j Context Initialization
**Symptom**: `HystrixRequestContext cannot be resolved`

**Problem**: Resilience4j doesn't require context initialization like Hystrix did.

**Fix**:
```java
// Wrong (Hystrix)
public class GetCommand extends BaseHystrixCommand {
    public GetCommand(...) {
        super(groupKey);
        HystrixRequestContext.initializeContext();  // Remove this
    }
}

// Correct (Resilience4j)
public class GetCommand extends BaseResilientCommand {
    public GetCommand(...) {
        super(groupKey);
        // No context initialization needed
    }
}
```

---

## 📚 Lessons Learned

### 1. Update Dependencies One at a Time
**Why**: Easier to isolate breaking changes.

**Process**:
1. Update dependency version in pom.xml
2. Try `mvn compile`
3. Fix errors
4. Commit
5. Move to next dependency

**Don't**: Update 20 dependencies at once. You'll spend hours debugging.

---

### 2. Read Migration Guides First
**Before updating**: Search for "[library name] migration guide"

**Example**: "Selenium 4 migration guide" saved hours by revealing API changes upfront.

**Time saved**: 50%+ when you know what to expect

---

### 3. Check Group ID Changes
**Common pattern**: Libraries change group IDs between major versions.

**Examples**:
- `com.atlassian.commonmark` → `org.commonmark`
- `javax.*` → `jakarta.*` (Java EE 8 → Jakarta EE 9)

**Check before updating**:
```bash
# Old way
mvn dependency:tree | grep old-group-id

# New way
mvn dependency:tree | grep new-group-id
```

---

### 4. Test Frameworks Have Different Semantics
**TestNG vs JUnit 5**:
- Different parameter order (assertEquals)
- Different lifecycle (BeforeClass must be static in JUnit 5)
- Different dependency injection (Listeners vs Extensions)
- Different test discovery (method naming conventions)

**Don't assume** one-to-one mapping. Read JUnit 5 docs carefully.

---

### 5. Virtual Threads Are Opt-In
**Gotcha**: Java 21 doesn't automatically use virtual threads.

**To enable**:
```java
// Traditional threads
ExecutorService executor = Executors.newFixedThreadPool(10);

// Virtual threads (Java 21+)
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

**When to use**: I/O-bound tasks (HTTP calls, database queries)
**When not to use**: CPU-bound tasks (still use regular threads)

---

### 6. Maven Surefire Auto-Detection
**Good news**: Surefire 3.x auto-detects JUnit 5.

**No need for**:
```xml
<!-- Not needed in Surefire 3.x -->
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-surefire-provider</artifactId>
</dependency>
```

**Just upgrade**:
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.2</version>
</plugin>
```

---

## 🔧 Best Practices

### 1. Compile After Each Fix
```bash
# Not this
# Fix 10 errors, then compile

# This
# Fix 1 error, compile, fix next error, compile
mvn compile -q
```

**Why**: Catch cascading errors early.

---

### 2. Use grep to Find All Usages
Before changing an API:
```bash
# Find all usages of old method
grep -rn "oldMethod()" src/

# Find all imports of old package
grep -rn "import old.package" src/
```

**Then**: Update all at once with sed or IDE refactoring.

---

### 3. Keep Old Code as .bak for Reference
```bash
cp OldClass.java OldClass.java.bak
# Make changes to OldClass.java
# If you need reference, check .bak file
```

**Don't commit .bak files**: Add to .gitignore.

---

### 4. Document Breaking Changes Immediately
When you fix a breaking change:
```markdown
## Breaking Changes

### Selenium 4 Migration
- Changed: `findElementById()` → `findElement(By.id())`
- Files: ObtainGitHubAccessToken.java (5 occurrences)
- Reason: Selenium 4 removed convenience methods
```

**Why**: Future you will thank present you.

---

### 5. Test Incrementally
```bash
# Not this
mvn test  # Run all 100 tests

# This
mvn test -Dtest=SingleTest  # Test one class
mvn test -Dtest="*Cache*"   # Test related classes
mvn test                     # Finally, run all
```

**Why**: Faster feedback loop.

---

## 🎯 Quick Reference: Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `package X does not exist` | Missing/wrong dependency | Check groupId, version |
| `cannot find symbol: method X` | API changed | Check migration guide |
| `cannot find symbol: class X` | Import wrong/removed | Update import or add dependency |
| `has been deprecated` | Using old API | Update to new API (or suppress) |
| `Unsupported class version` | Java version mismatch | Update java.version in pom.xml |
| `BUILD FAILURE` (no details) | Plugin issue | Run `mvn -X` for debug output |
| `Tests run: 0` | Test framework not detected | Update Surefire plugin |

---

## 🚀 Performance Tips

### Maven Build Speed
```bash
# Parallel builds
mvn clean install -T 4  # Use 4 threads

# Skip tests during dev
mvn compile -DskipTests

# Offline mode (if deps cached)
mvn compile -o

# Quiet mode
mvn compile -q
```

### IDE Tips
- **IntelliJ**: Enable "Compile on save" for instant feedback
- **IntelliJ**: Use "Optimize Imports" after group ID changes
- **IntelliJ**: Use "Refactor → Migrate" for framework migrations

---

## 📖 Migration Checklist

When updating a dependency:

- [ ] Read changelog/migration guide
- [ ] Check for group ID changes
- [ ] Check for package renames
- [ ] Check for API changes
- [ ] Search codebase for usages
- [ ] Update imports
- [ ] Update API calls
- [ ] Compile
- [ ] Run tests
- [ ] Document changes
- [ ] Commit with descriptive message

---

## 🔗 Useful Resources

### Documentation
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [Resilience4j User Guide](https://resilience4j.readme.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Selenium 4 Documentation](https://www.selenium.dev/documentation/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)

### Tools
- [Maven Central Search](https://search.maven.org/)
- [MVNRepository](https://mvnrepository.com/)
- [Can I Use (Java features)](https://docs.oracle.com/en/java/javase/)

---

## 📝 Contributing to This Document

When you encounter a new gotcha:

1. **Document it immediately** (while fresh in your mind)
2. **Include code examples** (before/after)
3. **Explain why it happens** (not just how to fix)
4. **Add to Quick Reference** (if applicable)
5. **Update skills** (if it's a common pattern)

---

**Remember**: Every gotcha you document saves hours for the next developer!

---

*This document is a living document. Update it whenever you discover new gotchas or learnings.*
