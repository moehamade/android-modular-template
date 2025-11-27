#!/usr/bin/env bash

# Android Modular Template - Simple Rebranding Script
# This script updates template.properties and replaces all package references.
# Current package (hardcoded): com.example.myapp

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ============================================================================
# Helper Functions
# ============================================================================

print_info() {
    echo -e "${BLUE}ℹ ${1}${NC}"
}

print_success() {
    echo -e "${GREEN}✓ ${1}${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ ${1}${NC}"
}

print_error() {
    echo -e "${RED}✗ ${1}${NC}"
}

print_header() {
    echo ""
    echo -e "${CYAN}════════════════════════════════════════${NC}"
    echo -e "${CYAN}  ${1}${NC}"
    echo -e "${CYAN}════════════════════════════════════════${NC}"
}

print_step() {
    echo -e "${CYAN}▶ ${1}${NC}"
}

# Validate package name format
validate_package_name() {
    local package=$1
    if [[ ! $package =~ ^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$ ]]; then
        print_error "Invalid package name format: $package"
        print_info "Package name must be in format: com.company.app (lowercase, dots, no spaces)"
        return 1
    fi
    return 0
}

# Validate project name format
validate_project_name() {
    local name=$1
    if [[ -z "$name" || "$name" =~ [[:space:]] ]]; then
        print_error "Invalid project name: '$name'"
        print_info "Project name must not be empty or contain spaces (use PascalCase)"
        return 1
    fi
    return 0
}

# ============================================================================
# Interactive Input
# ============================================================================

print_header "Android Modular Template - Simple Rebrand"
print_warning "This will modify your project. Commit changes first!"
echo ""

read -p "Enter new project name (PascalCase, e.g., MyAwesomeApp): " NEW_PROJECT_NAME
read -p "Enter new package name (e.g., com.mycompany.myapp): " NEW_PACKAGE
read -p "Enter app display name (or press Enter to use '$NEW_PROJECT_NAME'): " NEW_APP_NAME

# Default app name to project name if empty
if [[ -z "$NEW_APP_NAME" ]]; then
    NEW_APP_NAME="$NEW_PROJECT_NAME"
fi

# Validate inputs
validate_project_name "$NEW_PROJECT_NAME" || exit 1
validate_package_name "$NEW_PACKAGE" || exit 1

# Derive values
NEW_PROJECT_LOWERCASE=$(echo "$NEW_PROJECT_NAME" | tr '[:upper:]' '[:lower:]')
NEW_BASE_PACKAGE=$(echo "$NEW_PACKAGE" | sed 's/\.[^.]*$//')  # Remove last segment

# Hardcoded current values (for simplicity)
CURRENT_BASE_PACKAGE="com.example"
CURRENT_APP_PACKAGE="com.example.myapp"
CURRENT_PROJECT_NAME="MyApp"

# ============================================================================
# Confirmation
# ============================================================================

print_header "Rebranding Summary"
echo "Current project:  $CURRENT_PROJECT_NAME → $NEW_PROJECT_NAME"
echo "Current package:  $CURRENT_APP_PACKAGE → $NEW_PACKAGE"
echo "Base package:     $CURRENT_BASE_PACKAGE → $NEW_BASE_PACKAGE"
echo "Display name:     $NEW_APP_NAME"
echo ""

read -p "Continue with rebranding? [y/N]: " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    print_info "Rebranding cancelled."
    exit 0
fi

# ============================================================================
# Step 1: Update template.properties
# ============================================================================

print_header "Step 1: Updating template.properties"

if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS - requires empty string for backup extension
    sed -i '' "s/^project.name=.*/project.name=$NEW_PROJECT_NAME/" template.properties
    sed -i '' "s/^project.name.lowercase=.*/project.name.lowercase=$NEW_PROJECT_LOWERCASE/" template.properties
    sed -i '' "s/^app.display.name=.*/app.display.name=$NEW_APP_NAME/" template.properties
    sed -i '' "s|^package.app=.*|package.app=$NEW_PACKAGE|" template.properties
else
    # Linux - no backup extension needed
    sed -i "s/^project.name=.*/project.name=$NEW_PROJECT_NAME/" template.properties
    sed -i "s/^project.name.lowercase=.*/project.name.lowercase=$NEW_PROJECT_LOWERCASE/" template.properties
    sed -i "s/^app.display.name=.*/app.display.name=$NEW_APP_NAME/" template.properties
    sed -i "s|^package.app=.*|package.app=$NEW_PACKAGE|" template.properties
fi

print_success "Updated template.properties"

# ============================================================================
# Step 2: Replace Package References in Code
# ============================================================================

print_header "Step 2: Updating Package References"

# Find all Kotlin/Java files (exclude build and .git directories)
FILES=$(find . -type f \( -name "*.kt" -o -name "*.java" \) ! -path "*/build/*" ! -path "*/.git/*")

print_step "Replacing import statements..."

echo "$FILES" | while IFS= read -r file; do
    if [[ -n "$file" && -f "$file" ]]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # Replace imports (specific → general)
            sed -i '' "s|import ${CURRENT_APP_PACKAGE}|import ${NEW_PACKAGE}|g" "$file"
            sed -i '' "s|import ${CURRENT_BASE_PACKAGE}\\.feature|import ${NEW_BASE_PACKAGE}.feature|g" "$file"
            sed -i '' "s|import ${CURRENT_BASE_PACKAGE}\\.datastore|import ${NEW_BASE_PACKAGE}.datastore|g" "$file"
            sed -i '' "s|import ${CURRENT_BASE_PACKAGE}\\.|import ${NEW_BASE_PACKAGE}.|g" "$file"
        else
            # Linux version
            sed -i "s|import ${CURRENT_APP_PACKAGE}|import ${NEW_PACKAGE}|g" "$file"
            sed -i "s|import ${CURRENT_BASE_PACKAGE}\\.feature|import ${NEW_BASE_PACKAGE}.feature|g" "$file"
            sed -i "s|import ${CURRENT_BASE_PACKAGE}\\.datastore|import ${NEW_BASE_PACKAGE}.datastore|g" "$file"
            sed -i "s|import ${CURRENT_BASE_PACKAGE}\\.|import ${NEW_BASE_PACKAGE}.|g" "$file"
        fi
    fi
done

print_step "Replacing package declarations..."

echo "$FILES" | while IFS= read -r file; do
    if [[ -n "$file" && -f "$file" ]]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # Replace package declarations (specific → general)
            sed -i '' "s|^package ${CURRENT_APP_PACKAGE}|package ${NEW_PACKAGE}|g" "$file"
            sed -i '' "s|^package ${CURRENT_BASE_PACKAGE}\\.feature|package ${NEW_BASE_PACKAGE}.feature|g" "$file"
            sed -i '' "s|^package ${CURRENT_BASE_PACKAGE}\\.datastore|package ${NEW_BASE_PACKAGE}.datastore|g" "$file"
            sed -i '' "s|^package ${CURRENT_BASE_PACKAGE}\\.|package ${NEW_BASE_PACKAGE}.|g" "$file"
        else
            # Linux version
            sed -i "s|^package ${CURRENT_APP_PACKAGE}|package ${NEW_PACKAGE}|g" "$file"
            sed -i "s|^package ${CURRENT_BASE_PACKAGE}\\.feature|package ${NEW_BASE_PACKAGE}.feature|g" "$file"
            sed -i "s|^package ${CURRENT_BASE_PACKAGE}\\.datastore|package ${NEW_BASE_PACKAGE}.datastore|g" "$file"
            sed -i "s|^package ${CURRENT_BASE_PACKAGE}\\.|package ${NEW_BASE_PACKAGE}.|g" "$file"
        fi
    fi
done

print_success "Package references updated"

# ============================================================================
# Step 3: Update AndroidManifest.xml
# ============================================================================

print_header "Step 3: Updating AndroidManifest.xml"

MANIFEST_FILE="app/src/main/AndroidManifest.xml"

if [[ -f "$MANIFEST_FILE" ]]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s|${CURRENT_BASE_PACKAGE}\\.notifications\\.AppFCMService|${NEW_BASE_PACKAGE}.notifications.AppFCMService|g" "$MANIFEST_FILE"
    else
        sed -i "s|${CURRENT_BASE_PACKAGE}\\.notifications\\.AppFCMService|${NEW_BASE_PACKAGE}.notifications.AppFCMService|g" "$MANIFEST_FILE"
    fi
    print_success "AndroidManifest.xml updated"
else
    print_warning "AndroidManifest.xml not found at $MANIFEST_FILE"
fi

# ============================================================================
# Step 4: Rename Package Directories
# ============================================================================

print_header "Step 4: Renaming Package Directories"

# Convert package names to paths
CURRENT_BASE_PATH=$(echo "$CURRENT_BASE_PACKAGE" | tr '.' '/')
NEW_BASE_PATH=$(echo "$NEW_BASE_PACKAGE" | tr '.' '/')
CURRENT_APP_PATH=$(echo "$CURRENT_APP_PACKAGE" | tr '.' '/')
NEW_APP_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

print_step "Renaming app package directories..."

# 1. Rename app package (most specific)
find . -type d \( -path "*/src/main/java/$CURRENT_APP_PATH" -o -path "*/src/main/kotlin/$CURRENT_APP_PATH" \) 2>/dev/null | while IFS= read -r old_dir; do
    if [[ -n "$old_dir" && -d "$old_dir" ]]; then
        new_dir=$(echo "$old_dir" | sed "s|$CURRENT_APP_PATH|$NEW_APP_PATH|g")
        mkdir -p "$(dirname "$new_dir")"
        echo "  $old_dir → $new_dir"
        mv "$old_dir" "$new_dir" 2>/dev/null || true
    fi
done

print_step "Renaming base package directories..."

# 2. Rename base package directories (for all other modules)
find . -type d \( -path "*/src/main/java/$CURRENT_BASE_PATH" -o -path "*/src/main/kotlin/$CURRENT_BASE_PATH" \) 2>/dev/null | while IFS= read -r old_dir; do
    if [[ -n "$old_dir" && -d "$old_dir" ]]; then
        new_dir=$(echo "$old_dir" | sed "s|$CURRENT_BASE_PATH|$NEW_BASE_PATH|g")
        mkdir -p "$(dirname "$new_dir")"
        echo "  $old_dir → $new_dir"
        mv "$old_dir" "$new_dir" 2>/dev/null || true
    fi
done

# 3. Clean up empty directories
print_step "Cleaning up empty directories..."
find . -type d -empty -path "*/src/main/*/com/*" 2>/dev/null | while IFS= read -r empty_dir; do
    if [[ -n "$empty_dir" ]]; then
        rmdir "$empty_dir" 2>/dev/null || true
    fi
done

print_success "Package directories renamed"

# ============================================================================
# Step 5: Gradle Clean
# ============================================================================

print_header "Step 5: Cleaning Build"

print_step "Running ./gradlew clean..."

if ./gradlew clean 2>/dev/null; then
    print_success "Build cleaned successfully"
else
    print_warning "Clean failed (you may need to sync Gradle in Android Studio)"
fi

# ============================================================================
# Final Summary
# ============================================================================

print_header "Rebranding Complete!"

print_success "Project successfully rebranded to $NEW_PROJECT_NAME"
echo ""
echo -e "${GREEN}Summary of changes:${NC}"
echo "  Package:  $CURRENT_APP_PACKAGE → $NEW_PACKAGE"
echo "  Base:     $CURRENT_BASE_PACKAGE → $NEW_BASE_PACKAGE"
echo "  Project:  $CURRENT_PROJECT_NAME → $NEW_PROJECT_NAME"
echo "  App name: $NEW_APP_NAME"
echo ""

print_warning "Next steps:"
echo "  1. Open Android Studio and sync Gradle"
echo "  2. Review changes: git diff"
echo "  3. Setup Firebase:"
echo "     - Create project at https://console.firebase.google.com"
echo "     - Add Android app with package: $NEW_PACKAGE"
echo "     - Download google-services.json to app/ directory"
echo "  4. Build the app: ./gradlew :app:assembleDevDebug"
echo "  5. Commit changes: git add . && git commit -m 'Rebrand to $NEW_PROJECT_NAME'"
echo ""
print_success "Happy coding!"
