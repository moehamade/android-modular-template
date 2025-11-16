#!/bin/bash

# Install git hooks for the MyApp project
# Run this script once after cloning the repository

HOOKS_DIR=".githooks"
GIT_HOOKS_DIR=".git/hooks"

echo "🔧 Installing git hooks..."

# Check if .githooks directory exists
if [ ! -d "$HOOKS_DIR" ]; then
    echo "❌ Error: $HOOKS_DIR directory not found"
    exit 1
fi

# Create .git/hooks directory if it doesn't exist
mkdir -p "$GIT_HOOKS_DIR"

# Copy all hooks from .githooks to .git/hooks
for hook in "$HOOKS_DIR"/*; do
    if [ -f "$hook" ]; then
        hook_name=$(basename "$hook")
        cp "$hook" "$GIT_HOOKS_DIR/$hook_name"
        chmod +x "$GIT_HOOKS_DIR/$hook_name"
        echo "✓ Installed $hook_name"
    fi
done

echo ""
echo "✅ Git hooks installed successfully!"
echo ""
echo "Pre-commit checks will now run automatically before each commit."
echo "To skip hooks (not recommended): git commit --no-verify"
#!/bin/bash

# Pre-commit hook for MyApp Android project
# Runs code quality checks before allowing commits

set -e

echo "🔍 Running pre-commit checks..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get list of staged Kotlin files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.kt$|\.kts$' || true)

if [ -z "$STAGED_FILES" ]; then
    echo -e "${GREEN}✓ No Kotlin files to check${NC}"
    exit 0
fi

echo "📝 Found staged Kotlin files:"
echo "$STAGED_FILES"

# Run Detekt on staged files
echo ""
echo "🔍 Running Detekt..."
if ./gradlew detekt --daemon > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Detekt passed${NC}"
else
    echo -e "${RED}✗ Detekt found issues${NC}"
    echo ""
    echo "Please fix the issues reported by Detekt before committing."
    echo "Run: ./gradlew detekt"
    echo ""
    echo "To bypass this check (not recommended): git commit --no-verify"
    exit 1
fi

# Run unit tests (quick smoke test)
echo ""
echo "🧪 Running unit tests..."
if ./gradlew test --daemon > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Tests passed${NC}"
else
    echo -e "${RED}✗ Tests failed${NC}"
    echo ""
    echo "Please fix failing tests before committing."
    echo "Run: ./gradlew test"
    echo ""
    echo "To bypass this check (not recommended): git commit --no-verify"
    exit 1
fi

echo ""
echo -e "${GREEN}✓ All pre-commit checks passed!${NC}"
exit 0

