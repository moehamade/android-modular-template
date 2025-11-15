#!/usr/bin/env bash

# Enhanced Zencastr Rebranding Script
# This script comprehensively renames the entire project from Zencastr to your custom project name
# Handles: package names, class names, comments, documentation, Firebase setup

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

# Current values
CURRENT_PROJECT_NAME="Zencastr"
CURRENT_PACKAGE="com.acksession.zencastr"
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

# Function to convert PascalCase to kebab-case (if needed)
to_kebab_case() {
    echo "$1" | sed 's/\([A-Z]\)/-\1/g' | sed 's/^-//' | tr '[:upper:]' '[:lower:]'
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: ./rebrand.sh [OPTIONS]

Rebrand the Zencastr project to your custom project name.

OPTIONS:
    --project-name NAME      New project name (PascalCase, e.g., MyAwesomeApp)    --package-name PACKAGE   New package name (e.g., com.mycompany.myapp)    --app-name NAME          New app display name (e.g., "My Awesome App")    --dry-run                Preview changes without applying them    --reset-git              Remove git history and create fresh repository    -h, --help               Show this help message
INTERACTIVE MODE:
    Run without arguments for interactive prompts:    ./rebrand.sh
EXAMPLES:
    # Interactive mode    ./rebrand.sh
    # Command-line mode    ./rebrand.sh --project-name MyApp --package-name com.example.myapp --app-name "My App"
    # Dry run to preview changes    ./rebrand.sh --project-name MyApp --package-name com.example.myapp --app-name "My App" --dry-run
    # With git reset    ./rebrand.sh --project-name MyApp --package-name com.example.myapp --app-name "My App" --reset-git
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
    print_header "Enhanced Zencastr Rebranding Script"
    print_info "This script will comprehensively rename the entire project from Zencastr to your custom name."
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

# Derive lowercase and kebab-case versions
NEW_PROJECT_LOWERCASE=$(to_lowercase "$NEW_PROJECT_NAME")
CURRENT_PROJECT_LOWERCASE=$(to_lowercase "$CURRENT_PROJECT_NAME")

# Extract base package (for core modules) from CURRENT_PACKAGE
# CURRENT_PACKAGE is full app package: com.acksession.zencastr
# Extract base: com.acksession (everything before the last dot)
CURRENT_BASE_PACKAGE=$(echo "$CURRENT_PACKAGE" | sed 's/\.[^.]*$//')

# Extract base package from NEW_PACKAGE for core module replacements
# NEW_PACKAGE can be multi-level: com.vordead.echoworld
# Extract base: com.vordead (everything before the last dot)
NEW_BASE_PACKAGE=$(echo "$NEW_PACKAGE" | sed 's/\.[^.]*$//')

# Extract app suffix (last segment) for namespace handling in build.gradle.kts
# Example: com.acksession.zencastr → .zencastr
CURRENT_APP_SUFFIX=$(echo "$CURRENT_PACKAGE" | sed 's/.*\(\.[^.]*\)$/\1/')
# Example: com.vordead.echoworld → .echoworld
NEW_APP_SUFFIX=$(echo "$NEW_PACKAGE" | sed 's/.*\(\.[^.]*\)$/\1/')

# Extract package components for directory renaming
CURRENT_PACKAGE_PATH=$(echo "$CURRENT_PACKAGE" | tr '.' '/')
NEW_PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

# Show summary
print_header "Rebranding Summary"
echo "Current project (PascalCase): $CURRENT_PROJECT_NAME"
echo "New project (PascalCase):     $NEW_PROJECT_NAME"
echo ""
echo "Current project (lowercase):  $CURRENT_PROJECT_LOWERCASE"
echo "New project (lowercase):      $NEW_PROJECT_LOWERCASE"
echo ""
echo "Current app package:          $CURRENT_PACKAGE"
echo "New app package:              $NEW_PACKAGE"
echo ""
echo "Current base package:         $CURRENT_BASE_PACKAGE"
echo "New base package:             $NEW_BASE_PACKAGE"
echo ""
echo "Current app suffix:           $CURRENT_APP_SUFFIX"
echo "New app suffix:               $NEW_APP_SUFFIX"
echo ""
echo "Current app name:             $CURRENT_APP_NAME"
echo "New app name:                 $NEW_APP_NAME"
echo ""
echo "Dry run:                      $DRY_RUN"
echo "Reset git:                    $RESET_GIT"
echo ""

if [[ "$DRY_RUN" == false ]]; then
    read -p "Continue with rebranding? [y/N]: " confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        print_info "Rebranding cancelled."
        exit 0
    fi
fi

# Function to replace content in files
replace_in_files() {
    local pattern=$1
    local replacement=$2
    local file_pattern=$3
    local description=$4

    print_step "$description"

    # Find all matching files first
    local files=$(find . -type f -name "$file_pattern" ! -path "*/build/*" ! -path "*/.git/*" ! -path "*/.gradle/*" ! -path "*/rebrand.sh" ! -path "*.backup" 2>/dev/null)

    if [[ -z "$files" ]]; then
        print_info "No files found matching pattern"
        return
    fi

    # Filter files that contain the pattern
    local matching_files=$(echo "$files" | xargs grep -l "$pattern" 2>/dev/null || true)

    if [[ -z "$matching_files" ]]; then
        print_info "No files contain the pattern"
        return
    fi

    local count=$(echo "$matching_files" | wc -l | tr -d ' ')

    if [[ "$DRY_RUN" == true ]]; then
        # Dry run: show first 5 matches
        echo "$matching_files" | head -5 | while IFS= read -r file; do
            echo "  Would modify: $file"
        done
        if [[ $count -gt 5 ]]; then
            echo "  ... and $((count - 5)) more files"
        fi
        print_info "Would modify $count file(s)"
    else
        # Actual replacement
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            echo "$matching_files" | xargs sed -i '' "s|$pattern|$replacement|g" 2>/dev/null || true
        else
            # Linux
            echo "$matching_files" | xargs sed -i "s|$pattern|$replacement|g" 2>/dev/null || true
        fi
        print_success "Modified $count file(s)"
    fi
}

# Function to rename directories
rename_package_directories() {
    print_step "Renaming package directory structure..."

    local renamed_count=0

    # Step 1: Rename full app package directories first (most specific)
    # This handles app/src/main/java/com/acksession/zencastr -> com/vordead/echoworld
    find . -type d \( -path "*/src/main/java/$CURRENT_PACKAGE_PATH" -o -path "*/src/main/kotlin/$CURRENT_PACKAGE_PATH" \) 2>/dev/null > /tmp/app_dirs.txt

    while IFS= read -r old_dir; do
        if [[ -z "$old_dir" || ! -d "$old_dir" ]]; then
            continue
        fi

        local new_dir=$(echo "$old_dir" | sed "s|$CURRENT_PACKAGE_PATH|$NEW_PACKAGE_PATH|g")

        if [[ "$DRY_RUN" == true ]]; then
            echo "  Would rename: $old_dir -> $new_dir"
            ((renamed_count++))
        else
            mkdir -p "$(dirname "$new_dir")"
            echo "  Renaming: $old_dir -> $new_dir"
            mv "$old_dir" "$new_dir" 2>/dev/null && ((renamed_count++)) || true
        fi
    done < /tmp/app_dirs.txt
    rm -f /tmp/app_dirs.txt

    # Step 2: Rename base package directories (for core modules)
    # This handles core/*/src/main/java/com/acksession -> com/vordead
    local current_base_path=$(echo "$CURRENT_BASE_PACKAGE" | tr '.' '/')
    local new_base_path=$(echo "$NEW_BASE_PACKAGE" | tr '.' '/')

    find . -type d \( -path "*/src/main/java/$current_base_path" -o -path "*/src/main/kotlin/$current_base_path" \) 2>/dev/null > /tmp/base_dirs.txt

    while IFS= read -r old_dir; do
        if [[ -z "$old_dir" || ! -d "$old_dir" ]]; then
            continue
        fi

        local new_dir=$(echo "$old_dir" | sed "s|$current_base_path|$new_base_path|g")

        if [[ "$DRY_RUN" == true ]]; then
            echo "  Would rename: $old_dir -> $new_dir"
            ((renamed_count++))
        else
            mkdir -p "$(dirname "$new_dir")"
            echo "  Renaming: $old_dir -> $new_dir"
            mv "$old_dir" "$new_dir" 2>/dev/null && ((renamed_count++)) || true
        fi
    done < /tmp/base_dirs.txt
    rm -f /tmp/base_dirs.txt

    # Clean up empty old package directory trees
    if [[ "$DRY_RUN" == false ]]; then
        find . -type d -empty -path "*/src/main/*/com/*" 2>/dev/null | while IFS= read -r empty_dir; do
            rmdir "$empty_dir" 2>/dev/null || true
        done
    fi

    if [[ "$DRY_RUN" == true ]]; then
        print_info "Would rename $renamed_count director(ies)"
    else
        if [[ $renamed_count -gt 0 ]]; then
            print_success "Renamed $renamed_count director(ies)"
        else
            print_info "No directories needed renaming"
        fi
    fi
}

# Function to rename files containing project name
rename_project_files() {
    print_step "Renaming files containing project name..."

    # Find files with project name in filename and save to temp file
    find . -type f -name "*$CURRENT_PROJECT_NAME*" \
        ! -path "*/build/*" ! -path "*/.git/*" ! -path "*/.gradle/*" ! -path "*/rebrand.sh" ! -path "*.backup" \
        2>/dev/null > /tmp/files_to_rename.txt

    local total=$(wc -l < /tmp/files_to_rename.txt | tr -d ' ')

    if [[ $total -eq 0 ]]; then
        rm -f /tmp/files_to_rename.txt
        print_info "No files found with project name in filename"
        return
    fi

    local renamed_count=0

    while IFS= read -r old_file; do
        if [[ -z "$old_file" ]]; then
            continue
        fi

        # Get current directory and filename
        local old_dir=$(dirname "$old_file")
        local old_filename=$(basename "$old_file")
        local new_filename=$(echo "$old_filename" | sed "s|$CURRENT_PROJECT_NAME|$NEW_PROJECT_NAME|g")

        # Check if directory was renamed (contains old package path)
        local new_dir="$old_dir"
        if [[ "$old_dir" == *"$CURRENT_PACKAGE_PATH"* ]]; then
            # Directory was renamed, update path
            new_dir=$(echo "$old_dir" | sed "s|$CURRENT_PACKAGE_PATH|$NEW_PACKAGE_PATH|g")
        elif [[ "$old_dir" == *"/$CURRENT_BASE_PACKAGE/"* ]]; then
            # Base package directory was renamed (for core modules)
            new_dir=$(echo "$old_dir" | sed "s|/$CURRENT_BASE_PACKAGE/|/$NEW_BASE_PACKAGE/|g")
        fi

        local new_file="$new_dir/$new_filename"

        if [[ "$old_file" != "$new_file" ]]; then
            if [[ "$DRY_RUN" == true ]]; then
                echo "  Would move/rename: $old_file -> $new_file"
                renamed_count=$((renamed_count + 1))
            else
                # Create destination directory if needed
                mkdir -p "$new_dir"

                echo "  Moving/renaming: $old_file -> $new_file"
                mv "$old_file" "$new_file" 2>/dev/null && renamed_count=$((renamed_count + 1)) || true
            fi
        fi    done < /tmp/files_to_rename.txt

    rm -f /tmp/files_to_rename.txt

    if [[ $renamed_count -gt 0 ]]; then
        if [[ "$DRY_RUN" == true ]]; then
            print_info "Would move/rename $renamed_count file(s)"
            else
            print_success "Moved/renamed $renamed_count file(s)"
            fi
    else        print_info "No files needed renaming"
    fi
}

# Function to replace content in specific files (Fastfile, Appfile, etc.)
replace_in_specific_files() {
    local pattern=$1
    local replacement=$2
    local file_path=$3
    local description=$4

    print_step "$description"

    if [[ ! -f "$file_path" ]]; then
        print_info "File not found: $file_path"
        return
    fi

    # Check if file contains the pattern
    if ! grep -q "$pattern" "$file_path" 2>/dev/null; then
        print_info "No pattern found in file"
        return
    fi

    if [[ "$DRY_RUN" == true ]]; then
        echo "  Would modify: $file_path"
        print_info "Would modify 1 file(s)"
    else
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s|$pattern|$replacement|g" "$file_path" 2>/dev/null || true
        else
            sed -i "s|$pattern|$replacement|g" "$file_path" 2>/dev/null || true
        fi
        print_success "Modified 1 file(s)"
    fi
}

# Function to handle Firebase configuration
handle_firebase_config() {
    print_header "Firebase Configuration"

    local firebase_file="app/google-services.json"

    if [[ -f "$firebase_file" ]]; then
        if [[ "$DRY_RUN" == true ]]; then
            print_info "Would backup: $firebase_file -> $firebase_file.backup"
            print_info "Would create dummy google-services.json with new package names"
        else
            # Backup existing file
            print_step "Backing up existing google-services.json..."
            cp "$firebase_file" "$firebase_file.backup"
            print_success "Backed up to: $firebase_file.backup"

            # Create dummy google-services.json
            print_step "Creating dummy google-services.json for testing..."
            cat > "$firebase_file" << EOF
{
  "project_info": {    "project_number": "000000000000",    "project_id": "placeholder-project",    "storage_bucket": "placeholder-project.appspot.com"  },  "client": [    {      "client_info": {        "mobilesdk_app_id": "1:000000000000:android:0000000000000000",        "android_client_info": {          "package_name": "$NEW_PACKAGE"        }      },      "oauth_client": [],      "api_key": [        {          "current_key": "AIzaSyDummy-Key-For-Testing-Only"        }      ],      "services": {        "appinvite_service": {          "other_platform_oauth_client": []        }      }    },    {      "client_info": {        "mobilesdk_app_id": "1:000000000000:android:0000000000000001",        "android_client_info": {          "package_name": "$NEW_PACKAGE.debug"        }      },      "oauth_client": [],      "api_key": [        {          "current_key": "AIzaSyDummy-Key-For-Testing-Only"        }      ],      "services": {        "appinvite_service": {          "other_platform_oauth_client": []        }      }    },    {      "client_info": {        "mobilesdk_app_id": "1:000000000000:android:0000000000000002",        "android_client_info": {          "package_name": "$NEW_PACKAGE.dev"        }      },      "oauth_client": [],      "api_key": [        {          "current_key": "AIzaSyDummy-Key-For-Testing-Only"        }      ],      "services": {        "appinvite_service": {          "other_platform_oauth_client": []        }      }    },    {      "client_info": {        "mobilesdk_app_id": "1:000000000000:android:0000000000000003",        "android_client_info": {          "package_name": "$NEW_PACKAGE.dev.debug"        }      },      "oauth_client": [],      "api_key": [        {          "current_key": "AIzaSyDummy-Key-For-Testing-Only"        }      ],      "services": {        "appinvite_service": {          "other_platform_oauth_client": []        }      }    }  ],  "configuration_version": "1"}
EOF
            print_success "Created dummy google-services.json for testing"
            print_warning "This is a PLACEHOLDER file - Firebase features will NOT work!"
        fi
    else        print_info "No google-services.json found (expected for fresh clones)"
    fi
}

# Function to show Firebase setup instructions
show_firebase_instructions() {
    print_header "Firebase Setup Required"

    print_warning "IMPORTANT: The dummy google-services.json allows builds but Firebase features are DISABLED."
    echo ""
    print_info "To enable Firebase (Analytics, Crashlytics, FCM, Remote Config):"
    echo ""
    echo "1. Go to: https://console.firebase.google.com"
    echo "2. Create a new Firebase project (or use existing)"
    echo "3. Add Android apps with these package names:"
    echo ""
    echo "   ${CYAN}▶${NC} $NEW_PACKAGE"
    echo "   ${CYAN}▶${NC} $NEW_PACKAGE.debug"
    echo "   ${CYAN}▶${NC} $NEW_PACKAGE.dev"
    echo "   ${CYAN}▶${NC} $NEW_PACKAGE.dev.debug"
    echo ""
    echo "4. Download google-services.json for each app"
    echo "5. Merge all 4 configurations into one google-services.json"
    echo "   (see app/README_FIREBASE_SETUP.md for detailed instructions)"
    echo "6. Replace app/google-services.json with the real file"
    echo ""
    print_info "Your original Firebase config was backed up to: app/google-services.json.backup"
    echo ""
}

# Main rebranding process
print_header "Starting Rebranding Process"

# Step 0: Replace lowercase project name patterns FIRST (before packages change!)
# This catches: plugin IDs, database names, API URLs
# CRITICAL: Must run BEFORE Step 1 to catch all lowercase occurrences
print_header "Step 0: Replacing Lowercase Project Identifiers (Plugin IDs, Database, URLs)"
replace_in_files "$CURRENT_PROJECT_LOWERCASE\\.android\\." "$NEW_PROJECT_LOWERCASE.android." "*.kts" "Replacing plugin IDs in Gradle scripts..."
replace_in_files "\"$CURRENT_PROJECT_LOWERCASE\_database\"" "\"${NEW_PROJECT_LOWERCASE}_database\"" "*.kt" "Replacing database names in Kotlin files..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE\\.com" "$NEW_PROJECT_LOWERCASE.com" "*.kts" "Replacing API URLs in Gradle scripts..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE\\.com" "$NEW_PROJECT_LOWERCASE.com" "*.kt" "Replacing API URLs in Kotlin files..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE\\.com" "$NEW_PROJECT_LOWERCASE.com" "*.xml" "Replacing URLs in XML files..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE\\.com" "$NEW_PROJECT_LOWERCASE.com" "*.md" "Replacing URLs in documentation..."

# Step 1: Replace FULL app package (most specific - prevents conflicts!)
# This replaces: com.acksession.zencastr → com.vordead.echoworld
print_header "Step 1: Replacing App Package Names"
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.kt" "Replacing app package in Kotlin files..."
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.kts" "Replacing app package in Gradle Kotlin scripts..."
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.xml" "Replacing app package in XML files..."
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.gradle" "Replacing app package in Gradle files..."
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.pro" "Replacing app package in ProGuard rules..."
replace_in_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "*.md" "Replacing app package in documentation..."

# Step 2: Replace base package for core modules
# This replaces: com.acksession → com.vordead (base only, for :core modules)
print_header "Step 2: Replacing Base Package Names (Core Modules)"
replace_in_files "$CURRENT_BASE_PACKAGE" "$NEW_BASE_PACKAGE" "*.kt" "Replacing base package in Kotlin files..."
replace_in_files "$CURRENT_BASE_PACKAGE" "$NEW_BASE_PACKAGE" "*.kts" "Replacing base package in Gradle scripts..."
replace_in_files "$CURRENT_BASE_PACKAGE" "$NEW_BASE_PACKAGE" "*.xml" "Replacing base package in XML files..."
replace_in_files "$CURRENT_BASE_PACKAGE" "$NEW_BASE_PACKAGE" "*.gradle" "Replacing base package in Gradle files..."
replace_in_files "$CURRENT_BASE_PACKAGE" "$NEW_BASE_PACKAGE" "*.pro" "Replacing base package in ProGuard rules..."
replace_in_files "$CURRENT_BASE_PACKAGE" "$NEW_BASE_PACKAGE" "*.md" "Replacing base package in documentation..."

# Step 2b: Replace namespace app suffix in build.gradle.kts files
# This handles: namespace = "${AndroidConfig.NAMESPACE_PREFIX}.zencastr" → ".echoworld"
# IMPORTANT: Only replace in *.kts files to avoid changing other occurrences
print_header "Step 2b: Replacing Namespace App Suffix"
if [[ "$CURRENT_APP_SUFFIX" != "$NEW_APP_SUFFIX" ]]; then
    replace_in_files "$CURRENT_APP_SUFFIX\"" "$NEW_APP_SUFFIX\"" "*.kts" "Replacing namespace suffix in build scripts..."
fi

# Step 3: Replace PascalCase project names (class names, object names, etc.)
print_header "Step 3: Replacing PascalCase Project Names"
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.kt" "Replacing PascalCase names in Kotlin files..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.kts" "Replacing PascalCase names in Gradle scripts..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.xml" "Replacing PascalCase names in XML files..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.md" "Replacing PascalCase names in documentation..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.yml" "Replacing PascalCase names in YAML files..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.yaml" "Replacing PascalCase names in YAML files..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.properties" "Replacing PascalCase names in properties files..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.txt" "Replacing PascalCase names in text files..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.sh" "Replacing PascalCase names in shell scripts..."

# Step 4: Replace lowercase project names ONLY in specific contexts (URLs, deep links, themes)
# IMPORTANT: Exclude package patterns to avoid breaking imports
print_header "Step 4: Replacing Lowercase Project Names (URLs, Schemes, Themes)"
replace_in_files "$CURRENT_PROJECT_LOWERCASE" "$NEW_PROJECT_LOWERCASE" "*.xml" "Replacing lowercase names in XML files (schemes, URLs)..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE" "$NEW_PROJECT_LOWERCASE" "*.md" "Replacing lowercase names in documentation..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE" "$NEW_PROJECT_LOWERCASE" "*.yml" "Replacing lowercase names in YAML files..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE" "$NEW_PROJECT_LOWERCASE" "*.yaml" "Replacing lowercase names in YAML files..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE" "$NEW_PROJECT_LOWERCASE" "*.pro" "Replacing lowercase names in ProGuard rules..."
replace_in_files "$CURRENT_PROJECT_LOWERCASE" "$NEW_PROJECT_LOWERCASE" "*.txt" "Replacing lowercase names in text files..."
# Note: Skipping *.kt and *.kts for lowercase to prevent breaking already-replaced packages

# Step 5: Replace app name in strings.xml
print_header "Step 5: Replacing App Display Name"
replace_in_files "$CURRENT_APP_NAME" "$NEW_APP_NAME" "strings.xml" "Replacing app display name in strings.xml..."

# Step 6: Rename package directory structure
print_header "Step 6: Renaming Package Directories"
rename_package_directories

# Step 7: Process specific files (Fastfile, Appfile, .disabled files, etc.)
print_header "Step 7: Processing Special Files"
replace_in_specific_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "fastlane/Fastfile" "Replacing project name in Fastfile..."
replace_in_specific_files "$CURRENT_PACKAGE" "$NEW_PACKAGE" "fastlane/Appfile" "Replacing app package in Appfile..."
replace_in_files "$CURRENT_PROJECT_NAME" "$NEW_PROJECT_NAME" "*.disabled" "Replacing project name in disabled files..."

# Step 8: Rename files containing project name
print_header "Step 8: Renaming Files"
rename_project_files

# Step 9: Clean up directory structure issues
print_header "Step 9: Cleaning Up Directory Structure"

if [[ "$DRY_RUN" == false ]]; then
    print_step "Fixing any remaining directory structure issues..."

    # 1. Remove empty old package directories
    find . -type d -empty -path "*/src/main/*/com/*" 2>/dev/null | while IFS= read -r empty_dir; do
        rmdir "$empty_dir" 2>/dev/null || true
    done

    # 2. Fix any duplicate nested directories (like config/config, di/di)
    # Save to temp file to avoid subshell variable issues
    find . -type d -path "*/com/*/*" ! -path "*/build/*" 2>/dev/null > /tmp/dirs_to_check.txt

    fixed_count=0
    while IFS= read -r dir; do
        if [[ -z "$dir" || ! -d "$dir" ]]; then
            continue
        fi

        basename_dir=$(basename "$dir")
        parent_dir=$(dirname "$dir")
        parent_basename=$(basename "$parent_dir")

        # If directory name matches parent name, flatten it
        if [[ "$basename_dir" == "$parent_basename" ]]; then
            echo "  Fixing duplicate nested directory: $dir"
            # Move contents up one level if any exist
            if [ "$(ls -A "$dir" 2>/dev/null)" ]; then
                mv "$dir"/* "$parent_dir/" 2>/dev/null || true
            fi
            # Remove the duplicate directory
            rmdir "$dir" 2>/dev/null || true
            ((fixed_count++))
        fi
    done < /tmp/dirs_to_check.txt

    rm -f /tmp/dirs_to_check.txt

    if [[ $fixed_count -eq 0 ]]; then
        print_success "Directory structure is clean"
    else
        print_success "Fixed $fixed_count duplicate director(ies)"
        fi
else
    print_info "Skipped in dry-run mode"
fi

# Step 10: Handle Firebase configuration
if [[ "$DRY_RUN" == false ]]; then
    handle_firebase_config
fi

# Step 11: Clean build
if [[ "$DRY_RUN" == false ]]; then
    print_header "Step 11: Cleaning Build"
    print_step "Running ./gradlew clean..."

    if ./gradlew clean 2>/dev/null; then
        print_success "Build cleaned successfully"
    else
        print_warning "Clean failed (this is OK if gradle wrapper needs updating)"
    fi
fi

# Step 12: Optional git reset
if [[ "$RESET_GIT" == true && "$DRY_RUN" == false ]]; then
    print_header "Step 12: Resetting Git History"
    print_warning "This will remove all git history and create a fresh repository!"
    read -r -p "Are you sure? [y/N]: " git_confirm

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
    echo ""
    echo "${GREEN}Summary of changes:${NC}"
    echo "  Package:     $CURRENT_PACKAGE → $NEW_PACKAGE"
    echo "  Project:     $CURRENT_PROJECT_NAME → $NEW_PROJECT_NAME"
    echo "  App name:    $CURRENT_APP_NAME → $NEW_APP_NAME"
    echo ""

    # Show Firebase instructions
    show_firebase_instructions

    print_warning "Next steps:"
    echo "  1. Review the changes with 'git diff'"
    echo "  2. Set up Firebase with your new package names (see instructions above)"
    echo "  3. Test the app: ./gradlew :app:assembleDevDebug"
    echo "  4. Commit the changes: git add . && git commit -m 'Rebrand to $NEW_PROJECT_NAME'"
    echo ""
    print_success "Happy coding! 🚀"
fi
