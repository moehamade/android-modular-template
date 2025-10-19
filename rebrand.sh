#!/usr/bin/env bash

# Zencastr Rebranding Script
# This script renames the entire project from Zencastr to your custom project name

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
DRY_RUN=false
RESET_GIT=false

# Current values
CURRENT_PROJECT_NAME="Zencastr"
CURRENT_PACKAGE="com.acksession"
CURRENT_APP_NAME="Zencastr"

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
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}  ${1}${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
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
        print_info "Project name must not be empty or contain spaces"
        return 1
    fi
    return 0
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: ./rebrand.sh [OPTIONS]

Rebrand the Zencastr project to your custom project name.

OPTIONS:
    --project-name NAME      New project name (PascalCase, e.g., MyAwesomeApp)
    --package-name PACKAGE   New package name (e.g., com.mycompany.myapp)
    --app-name NAME          New app display name (e.g., "My Awesome App")
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
    ./rebrand.sh --project-name MyApp --package-name com.example.myapp --app-name "My App"

    # Dry run to preview changes
    ./rebrand.sh --project-name MyApp --package-name com.example.myapp --app-name "My App" --dry-run

    # With git reset
    ./rebrand.sh --project-name MyApp --package-name com.example.myapp --app-name "My App" --reset-git

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
    print_header "Zencastr Rebranding Script"
    print_info "This script will rename the entire project from Zencastr to your custom name."
    print_warning "Make sure you have committed all changes before running this script!"
    echo ""

    read -p "Enter new project name (PascalCase, e.g., MyAwesomeApp): " NEW_PROJECT_NAME
    read -p "Enter new package name (e.g., com.mycompany.myapp): " NEW_PACKAGE
    read -p "Enter new app display name (e.g., \"My Awesome App\"): " NEW_APP_NAME

    echo ""
    read -p "Reset git history? (creates fresh repository) [y/N]: " reset_choice
    if [[ "$reset_choice" =~ ^[Yy]$ ]]; then
        RESET_GIT=true
    fi
fi

# Validate inputs
validate_project_name "$NEW_PROJECT_NAME" || exit 1
validate_package_name "$NEW_PACKAGE" || exit 1

# Show summary
print_header "Rebranding Summary"
echo "Current project: $CURRENT_PROJECT_NAME"
echo "New project:     $NEW_PROJECT_NAME"
echo ""
echo "Current package: $CURRENT_PACKAGE"
echo "New package:     $NEW_PACKAGE"
echo ""
echo "Current app name: $CURRENT_APP_NAME"
echo "New app name:     $NEW_APP_NAME"
echo ""
echo "Dry run:          $DRY_RUN"
echo "Reset git:        $RESET_GIT"
echo ""

if [[ "$DRY_RUN" == false ]]; then
    read -p "Continue with rebranding? [y/N]: " confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        print_info "Rebranding cancelled."
        exit 0
    fi
fi

# Extract package components
CURRENT_PACKAGE_PATH=$(echo "$CURRENT_PACKAGE" | tr '.' '/')
NEW_PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

# Function to replace content in files
replace_in_files() {
    local pattern=$1
    local replacement=$2
    local file_pattern=$3
    local description=$4

    print_info "$description"

    if [[ "$DRY_RUN" == true ]]; then
        # Dry run: show what would be changed
        find . -type f -name "$file_pattern" ! -path "*/build/*" ! -path "*/.git/*" ! -path "*/.gradle/*" ! -path "*/rebrand.sh" -exec grep -l "$pattern" {} \; 2>/dev/null | while read -r file; do
            echo "  Would modify: $file"
        done
    else
        # Actual replacement
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            find . -type f -name "$file_pattern" ! -path "*/build/*" ! -path "*/.git/*" ! -path "*/.gradle/*" ! -path "*/rebrand.sh" -exec sed -i '' "s|$pattern|$replacement|g" {} \; 2>/dev/null || true
        else
            # Linux
            find . -type f -name "$file_pattern" ! -path "*/build/*" ! -path "*/.git/*" ! -path "*/.gradle/*" ! -path "*/rebrand.sh" -exec sed -i "s|$pattern|$replacement|g" {} \; 2>/dev/null || true
        fi
    fi
}

# Function to rename directories
rename_package_directories() {
    print_info "Renaming package directory structure..."

    # Find all directories matching the old package structure
    local dirs=$(find . -type d -path "*src/main/java/$CURRENT_PACKAGE_PATH*" -o -path "*src/main/kotlin/$CURRENT_PACKAGE_PATH*" 2>/dev/null)

    if [[ -z "$dirs" ]]; then
        print_warning "No package directories found to rename"
        return
    fi

    while IFS= read -r old_dir; do
        if [[ -z "$old_dir" ]]; then
            continue
        fi

        # Calculate new directory path
        local new_dir=$(echo "$old_dir" | sed "s|$CURRENT_PACKAGE_PATH|$NEW_PACKAGE_PATH|g")

        if [[ "$DRY_RUN" == true ]]; then
            echo "  Would rename: $old_dir -> $new_dir"
        else
            # Create parent directory if it doesn't exist
            local parent_dir=$(dirname "$new_dir")
            mkdir -p "$parent_dir"

            # Move directory
            if [[ -d "$old_dir" && "$old_dir" != "$new_dir" ]]; then
                echo "  Renaming: $old_dir -> $new_dir"
                mv "$old_dir" "$new_dir"
            fi
        fi
    done <<< "$dirs"

    # Clean up empty old package directories
    if [[ "$DRY_RUN" == false ]]; then
        find . -type d -path "*src/main/java/$CURRENT_PACKAGE_PATH*" -o -path "*src/main/kotlin/$CURRENT_PACKAGE_PATH*" 2>/dev/null | while IFS= read -r dir; do
            if [[ -d "$dir" && -z "$(ls -A "$dir")" ]]; then
                rmdir "$dir" 2>/dev/null || true
            fi
        done
    fi
}

# Main rebranding process
print_header "Starting Rebranding Process"

# Step 1: Replace package names in all files
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.kt" "Replacing package names in Kotlin files..."
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.kts" "Replacing package names in Gradle Kotlin scripts..."
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.xml" "Replacing package names in XML files..."

# Step 2: Replace project names
replace_in_files "zencastr" "$(echo "$NEW_PROJECT_NAME" | tr '[:upper:]' '[:lower:]')" "*.kt" "Replacing lowercase project name in Kotlin files..."
replace_in_files "zencastr" "$(echo "$NEW_PROJECT_NAME" | tr '[:upper:]' '[:lower:]')" "*.kts" "Replacing lowercase project name in Gradle scripts..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.md" "Replacing project name in documentation..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.yml" "Replacing project name in YAML files..."

# Step 3: Replace app name in strings.xml
replace_in_files "$CURRENT_APP_NAME" "$NEW_APP_NAME" "strings.xml" "Replacing app display name in strings.xml..."

# Step 4: Rename package directory structure
rename_package_directories

# Step 5: Run build validation
if [[ "$DRY_RUN" == false ]]; then
    print_header "Validating Build"
    print_info "Running './gradlew clean build' to verify everything compiles..."

    if ./gradlew clean build; then
        print_success "Build successful! Project has been rebranded correctly."
    else
        print_error "Build failed! Please review the changes and fix any issues."
        exit 1
    fi
fi

# Step 6: Optional git reset
if [[ "$RESET_GIT" == true && "$DRY_RUN" == false ]]; then
    print_header "Resetting Git History"
    print_warning "This will remove all git history and create a fresh repository!"
    read -p "Are you sure? [y/N]: " git_confirm

    if [[ "$git_confirm" =~ ^[Yy]$ ]]; then
        rm -rf .git
        git init
        git add .
        git commit -m "Initial commit: $NEW_PROJECT_NAME"
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
    print_success "Project successfully rebranded from $CURRENT_PROJECT_NAME to $NEW_PROJECT_NAME"
    print_info "Package: $CURRENT_PACKAGE → $NEW_PACKAGE"
    print_info "App name: $CURRENT_APP_NAME → $NEW_APP_NAME"
    echo ""
    print_warning "Next steps:"
    echo "  1. Review the changes with 'git diff'"
    echo "  2. Test the app thoroughly"
    echo "  3. Update any remaining custom configurations"
    echo "  4. Commit the changes: git add . && git commit -m 'Rebrand to $NEW_PROJECT_NAME'"
fi
