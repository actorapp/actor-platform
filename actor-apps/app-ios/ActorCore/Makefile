.SUFFIXES: .java .m

ifdef CONFIGURATION_BUILD_DIR
# In Xcode build
BUILD_DIR = $(CONFIGURATION_BUILD_DIR)/build
PRODUCT_DIR = $(BUILT_PRODUCTS_DIR)
ARCHFLAGS = $(ARCHS:%=-arch %)
SDKFLAGS = --sysroot $(SDKROOT)
APPS_ROOT = $(abspath $(PROJECT_DIR)/..)
else
# In command-line build
BUILD_DIR = Build
PRODUCT_DIR = $(BUILD_DIR)
ARCHFLAGS =
SDKFLAGS =
APPS_ROOT = $(abspath ../..)
endif

ifdef OPTIMIZATION_LEVEL
DEBUGFLAGS := $(DEBUGFLAGS) -O$(OPTIMIZATION_LEVEL)
endif

ifdef OTHER_CFLAGS
DEBUGFLAGS := $(DEBUGFLAGS) $(OTHER_CFLAGS)
endif

# Workaround for iPhoneSimulator SDK's gcc bug
ifdef EFFECTIVE_PLATFORM_NAME
ifneq ($(EFFECTIVE_PLATFORM_NAME), -iphonesimulator)
WARNINGS := $(WARNINGS) -Wreturn-type
endif
endif

null :=
space := ${null} ${null}

J2OBJC_DISTRIBUTION = $(APPS_ROOT)/build-tools/dist/j2objc-0.9.8.2
J2OBJC = $(J2OBJC_DISTRIBUTION)/j2objc
J2OBJCC = $(J2OBJC_DISTRIBUTION)/j2objcc
J2OBJC_ARGS = -use-arc --generate-deprecated --doc-comments -g --static-accessor-methods
J2OBJCC_ARGS = -ObjC -fobjc-arc -fembed-bitcode-marker -Wobjc-designated-initializers -g $(ARCHFLAGS) $(SDKFLAGS) -I$(J2OBJC_DISTRIBUTION)/include

CC_ARM7 = -arch armv7 --sysroot=$(IOS_SDK_ROOT)
CC_ARM7S = -arch armv7s --sysroot=$(IOS_SDK_ROOT)
CC_ARM64 = -arch arm64 --sysroot=$(IOS_SDK_ROOT)
CC_I386 = -arch i386 --sysroot=$(IOS_SIMULATOR_SDK_ROOT)

LIBRARY_NAME = libactor.so

SOURCE_ROOTS = \
	$(APPS_ROOT)/runtime/src/main/java \
	$(APPS_ROOT)/runtime/src/generic/java \
	$(APPS_ROOT)/runtime-cocoa/src/main/java \
	$(APPS_ROOT)/core/src/main/java \
    $(APPS_ROOT)/core/src/ext/java \
	$(APPS_ROOT)/core-cocoa/src/main/java

EXCLUDED_CLASSES = \
	com.google.j2objc.annotations.ObjectiveCName \
    com.google.j2objc.annotations.Propery

PREFIXES = \
	--prefixes $(APPS_ROOT)/runtime-cocoa/src/main/prefixes.properties \
	--prefixes $(APPS_ROOT)/core-cocoa/src/main/prefixes.properties

SOURCES_DIR = Sources

EXCLUDED_FILES	= $(foreach class,$(EXCLUDED_CLASSES),$(subst .,/,$(class)).java)
JAVA_FILES = $(filter-out $(EXCLUDED_FILES),$(foreach dir,$(SOURCE_ROOTS),$(shell find $(dir) -name "*.java" | awk -F '$(dir)/' '{print $$NF}')))
OBJC_FILES = $(addprefix $(SOURCES_DIR)/, $(JAVA_FILES:.java=.m))
OBJECT_FILES = $(addprefix $(BUILD_DIR)/, $(JAVA_FILES:.java=.o))
SOURCE_PATHS = $(subst $(space),:,$(SOURCE_ROOTS))

UPDATED_SOURCES = $(BUILD_DIR)/.sources.list

# Build Tasks

# default:
# 	echo $(SDKROOT)

default: | translate build
	
# Translating files

translate: | $(SOURCES_DIR) $(BUILD_DIR) pre_translate $(OBJC_FILES)
	@if [ `cat $(UPDATED_SOURCES) | wc -l` -ge 1 ] ; then \
	$(J2OBJC) $(J2OBJC_ARGS) $(PREFIXES) -sourcepath $(SOURCE_PATHS) -d $(SOURCES_DIR) \
	`cat $(UPDATED_SOURCES)` ; \
	fi

pre_translate:
	@rm -f $(UPDATED_SOURCES)
	@touch $(UPDATED_SOURCES)

define SOURCE_ROOT_RULE
$(SOURCES_DIR)/%.m: $1/%.java
	@echo $$? >> $$(UPDATED_SOURCES)
endef
$(foreach root,$(SOURCE_ROOTS),$(eval $(call SOURCE_ROOT_RULE,$(root))))

# Compile files
build: $(BUILD_DIR) $(OBJECT_FILES)
	@rm -f $(PRODUCT_DIR)/$(LIBRARY_NAME)
	@ar rcs $(PRODUCT_DIR)/$(LIBRARY_NAME) $(OBJECT_FILES)
	@ranlib $(PRODUCT_DIR)/$(LIBRARY_NAME)

$(BUILD_DIR)/%.o: $(SOURCES_DIR)/%.m
	@mkdir -p $(dir $@)
	$(J2OBJCC) $(J2OBJCC_ARGS) -I$(SOURCES_DIR) -c $? -o $@

# Dirs
$(BUILD_DIR):
	@mkdir $(BUILD_DIR)

$(SOURCES_DIR):
	@mkdir $(SOURCES_DIR)	

clean:
	@rm -rf $(SOURCES_DIR) $(BUILD_DIR)