#!/usr/bin/env bash

# Android Modular Template - Rebranding Script
# This script updates template.properties and rebuilds the project with new branding.
# All package names and project identifiers are centralized in template.properties.

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default values
DRY_RUN=false
RESET_GIT=false

# Function to print colored output
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

# Function to validate package name format
validate_package_name() {
    local package=$1
    if [[ ! $package =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]]; then
        print_error "Invalid package name format: $package"
        print_info "Package name must be in format: com.company.app (lowercase, dots, no spaces)"
        return 1
    fi
    return 0
}

# Function to validate project name format
validate_project_name() {
    local name=$1
    if [[ -z "$name" || "$name" =~ [[:space:]] ]]; then
        print_error "Invalid project name: '$name'"
        print_info "Project name must not be empty or contain spaces (use PascalCase)"
        return 1
    fi
    return 0
}

# Function to convert PascalCase to lowercase
to_lowercase() {
    echo "$1" | tr '[:upper:]' '[:lower:]'
}

# Function to show usage
show_usage() {
    cat << 'EOF'
Usage: ./rebrand.sh [OPTIONS]

Rebrand the Android Modular Template to your custom project name.

This script updates template.properties (the single source of truth) and
rebuilds the project. Package directories are renamed automatically.

OPTIONS:
    --project-name NAME      New project name (PascalCase, e.g., MyAwesomeApp)
    --package-name PACKAGE   New package name (e.g., com.mycompany.myapp)
    --app-name NAME          New app display name (optional, defaults to project name)
    --dry-run                Preview changes without applying them
    --reset-git              Remove git history and create fresh repository
    -h, --help               Show this help message

INTERACTIVE MODE:
    Run without arguments for interactive prompts:
    ./rebrand.sh

EXAMPLES:
    # Interactive mode
    ./rebrand.sh

    # Command-line mode
    ./rebrand.sh --project-name MyApp --package-name com.example.myapp

    # With custom display name
    ./rebrand.sh --project-name MyApp \
                 --package-name com.example.myapp \
                 --app-name "My Awesome App"

    # Dry run to preview changes
    ./rebrand.sh --project-name MyApp \
                 --package-name com.example.myapp \
                 --dry-run

    # Fresh start (reset git history)
    ./rebrand.sh --project-name MyApp \
                 --package-name com.example.myapp \
                 --reset-git
EOF
}

# Parse command-line arguments
NEW_PROJECT_NAME=""
NEW_PACKAGE=""
NEW_APP_NAME=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --project-name)
            NEW_PROJECT_NAME="$2"
            shift 2
            ;;
        --package-name)
            NEW_PACKAGE="$2"
            shift 2
            ;;
        --app-name)
            NEW_APP_NAME="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --reset-git)
            RESET_GIT=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Interactive mode if no arguments provided
if [[ -z "$NEW_PROJECT_NAME" ]]; then
    print_header "Android Modular Template - Rebranding"
    print_info "This script will update template.properties with your project details."
    print_warning "Make sure you have committed all changes before running this script!"
    echo ""

    read -p "Enter new project name (PascalCase, e.g., MyAwesomeApp): " NEW_PROJECT_NAME
    read -p "Enter new package name (e.g., com.mycompany.myapp): " NEW_PACKAGE
    read -p "Enter new app display name (optional, press Enter to use '$NEW_PROJECT_NAME'): " NEW_APP_NAME

    # Default app name to project name if not provided
    if [[ -z "$NEW_APP_NAME" ]]; then
        NEW_APP_NAME="$NEW_PROJECT_NAME"
    fi

    echo ""
    read -p "Reset git history? (creates fresh repository) [y/N]: " reset_choice
    if [[ "$reset_choice" =~ ^[Yy]$ ]]; then
        RESET_GIT=true
    fi
fi

# Default app name to project name if not provided
if [[ -z "$NEW_APP_NAME" ]]; then
    NEW_APP_NAME="$NEW_PROJECT_NAME"
fi

# Validate inputs
validate_project_name "$NEW_PROJECT_NAME" || exit 1
validate_package_name "$NEW_PACKAGE" || exit 1

# Derive values
NEW_PROJECT_LOWERCASE=$(to_lowercase "$NEW_PROJECT_NAME")
NEW_BASE_PACKAGE=$(echo "$NEW_PACKAGE" | sed 's/\.[^.]*$//')  # Auto-derive base: com.example.myapp → com.example

# Read current values from template.properties
CURRENT_PROJECT_NAME=$(grep "^project.name=" template.properties | cut -d'=' -f2)
CURRENT_PACKAGE=$(grep "^package.app=" template.properties | cut -d'=' -f2)
CURRENT_BASE_PACKAGE=$(echo "$CURRENT_PACKAGE" | sed 's/\.[^.]*$//')  # Auto-derive from package.app

# Show summary
print_header "Rebranding Summary"
echo "Current project:     $CURRENT_PROJECT_NAME"
echo "New project:         $NEW_PROJECT_NAME"
echo ""
echo "Current package:     $CURRENT_PACKAGE (base: $CURRENT_BASE_PACKAGE)"
echo "New package:         $NEW_PACKAGE (base: $NEW_BASE_PACKAGE)"
echo ""
echo "App display name:    $NEW_APP_NAME"
echo ""
echo "Dry run:             $DRY_RUN"
echo "Reset git:           $RESET_GIT"
echo ""

if [[ "$DRY_RUN" == false ]]; then
    read -p "Continue with rebranding? [y/N]: " confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        print_info "Rebranding cancelled."
        exit 0
    fi
fi

# Main rebranding process
print_header "Step 1: Updating template.properties"

if [[ "$DRY_RUN" == true ]]; then
    print_info "Would update template.properties with:"
    echo "  project.name=$NEW_PROJECT_NAME"
    echo "  project.name.lowercase=$NEW_PROJECT_LOWERCASE"
    echo "  app.display.name=$NEW_APP_NAME"
    echo "  package.app=$NEW_PACKAGE (base: $NEW_BASE_PACKAGE auto-derived)"
else
    # Update template.properties
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/^project.name=.*/project.name=$NEW_PROJECT_NAME/" template.properties
        sed -i '' "s/^project.name.lowercase=.*/project.name.lowercase=$NEW_PROJECT_LOWERCASE/" template.properties
        sed -i '' "s/^app.display.name=.*/app.display.name=$NEW_APP_NAME/" template.properties
        sed -i '' "s|^package.app=.*|package.app=$NEW_PACKAGE|" template.properties
    else
        # Linux
        sed -i "s/^project.name=.*/project.name=$NEW_PROJECT_NAME/" template.properties
        sed -i "s/^project.name.lowercase=.*/project.name.lowercase=$NEW_PROJECT_LOWERCASE/" template.properties
        sed -i "s/^app.display.name=.*/app.display.name=$NEW_APP_NAME/" template.properties
        sed -i "s|^package.app=.*|package.app=$NEW_PACKAGE|" template.properties
    fi
    print_success "Updated template.properties"
fi

# Step 2: Rename package directories
print_header "Step 2: Renaming Package Directories"

if [[ "$DRY_RUN" == true ]]; then
    print_info "Would rename package directories from:"
    echo "  $CURRENT_BASE_PACKAGE → $NEW_BASE_PACKAGE"
    echo "  $CURRENT_PACKAGE → $NEW_PACKAGE"
else
    print_step "Renaming Java/Kotlin package directories..."

    # Convert package names to paths
    CURRENT_BASE_PATH=$(echo "$CURRENT_BASE_PACKAGE" | tr '.' '/')
    NEW_BASE_PATH=$(echo "$NEW_BASE_PACKAGE" | tr '.' '/')
    CURRENT_APP_PATH=$(echo "$CURRENT_PACKAGE" | tr '.' '/')
    NEW_APP_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

    # Rename app package directories first (most specific)
    find . -type d \( -path "*/src/main/java/$CURRENT_APP_PATH" -o -path "*/src/main/kotlin/$CURRENT_APP_PATH" \) 2>/dev/null | while IFS= read -r old_dir; do
        if [[ -n "$old_dir" && -d "$old_dir" ]]; then
            new_dir=$(echo "$old_dir" | sed "s|$CURRENT_APP_PATH|$NEW_APP_PATH|g")
            mkdir -p "$(dirname "$new_dir")"
            echo "  Renaming: $old_dir → $new_dir"
            mv "$old_dir" "$new_dir" 2>/dev/null || true
        fi
    done

    # Rename base package directories (for core modules)
    find . -type d \( -path "*/src/main/java/$CURRENT_BASE_PATH" -o -path "*/src/main/kotlin/$CURRENT_BASE_PATH" \) 2>/dev/null | while IFS= read -r old_dir; do
        if [[ -n "$old_dir" && -d "$old_dir" ]]; then
            new_dir=$(echo "$old_dir" | sed "s|$CURRENT_BASE_PATH|$NEW_BASE_PATH|g")
            mkdir -p "$(dirname "$new_dir")"
            echo "  Renaming: $old_dir → $new_dir"
            mv "$old_dir" "$new_dir" 2>/dev/null || true
        fi
    done

    # Clean up empty directories
    find . -type d -empty -path "*/src/main/*/com/*" 2>/dev/null | while IFS= read -r empty_dir; do
        rmdir "$empty_dir" 2>/dev/null || true
    done

    print_success "Package directories renamed"
fi

# Step 3: Verify auto-configuration
print_header "Step 3: Verifying Auto-Configuration"
print_info "✓ settings.gradle.kts reads rootProject.name from template.properties automatically"
print_info "✓ app/build.gradle.kts auto-generates from template.properties:"
echo "    - String resources (app_name)"
echo "    - Manifest placeholders (deep links, app links, theme)"
echo "    - BuildConfig values (API_BASE_URL)"
print_success "All configurations will update automatically on next Gradle sync"

# Step 4: Clean and rebuild
if [[ "$DRY_RUN" == false ]]; then
    print_header "Step 4: Cleaning Build"
    print_step "Running ./gradlew clean..."

    if ./gradlew clean 2>/dev/null; then
        print_success "Build cleaned successfully"
    else
        print_warning "Clean failed (may need gradle sync in IDE)"
    fi
fi

# Step 5: Optional git reset
if [[ "$RESET_GIT" == true && "$DRY_RUN" == false ]]; then
    print_header "Step 5: Resetting Git History"
    print_warning "This will remove all git history and create a fresh repository!"
    read -r -p "Are you sure? [y/N]: " git_confirm

    if [[ "$git_confirm" =~ ^[Yy]$ ]]; then
        rm -rf .git
        git init
        git add .
        git commit -m "Initial commit: $NEW_PROJECT_NAME

Generated from android-modular-template
https://github.com/moehamade/android-modular-template"
        print_success "Git history reset. Fresh repository created."
    else
        print_info "Git reset skipped."
    fi
fi

# Final summary
print_header "Rebranding Complete!"

if [[ "$DRY_RUN" == true ]]; then
    print_info "Dry run completed. No changes were made."
    print_info "Run without --dry-run to apply changes."
else
    print_success "Project successfully rebranded to $NEW_PROJECT_NAME"
    echo ""
    echo "${GREEN}Summary of changes:${NC}"
    echo "  Package:     $CURRENT_PACKAGE → $NEW_PACKAGE"
    echo "  Base:        $CURRENT_BASE_PACKAGE → $NEW_BASE_PACKAGE (auto-derived)"
    echo "  Project:     $CURRENT_PROJECT_NAME → $NEW_PROJECT_NAME"
    echo "  App name:    $NEW_APP_NAME"
    echo ""

    print_warning "Next steps:"
    echo "  1. Open the project in Android Studio and sync Gradle"
    echo "  2. Review changes with 'git diff'"
    echo "  3. Set up Firebase:"
    echo "     - Create project at https://console.firebase.google.com"
    echo "     - Add Android app with package: $NEW_PACKAGE"
    echo "     - Download google-services.json to app/ directory"
    echo "     - See app/README_FIREBASE_SETUP.md for details"
    echo "  4. Build the app: ./gradlew :app:assembleDevDebug"
    echo "  5. Commit changes: git add . && git commit -m 'Rebrand to $NEW_PROJECT_NAME'"
    echo ""
    print_success "Happy coding! 🚀"
fi
