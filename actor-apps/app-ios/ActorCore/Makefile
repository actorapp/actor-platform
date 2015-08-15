.SUFFIXES: .java .m

null :=
space := ${null} ${null}

# J2OBJC Configuration
APPS_ROOT = $(abspath ../..)
J2OBJC_DISTRIBUTION = $(APPS_ROOT)/build-tools/dist/j2objc-0.9.8
J2OBJC = $(J2OBJC_DISTRIBUTION)/j2objc
J2OBJCC = $(J2OBJC_DISTRIBUTION)/j2objcc
J2OBJC_ARGS = -use-arc --generate-deprecated --doc-comments -g
J2OBJCC_ARGS = -ObjC -fobjc-arc -g

LIBRARY_NAME = libactor.so

SOURCE_ROOTS = \
	$(APPS_ROOT)/runtime/src/main/java \
	$(APPS_ROOT)/runtime/src/generic/java \
	$(APPS_ROOT)/runtime-cocoa/src/main/java \
	$(APPS_ROOT)/core/src/main/java \
	$(APPS_ROOT)/core-cocoa/src/main/java

EXCLUDED_CLASSES = \
	com.google.j2objc.annotations.ObjectiveCName

PREFIXES = \
	--prefixes $(APPS_ROOT)/runtime-cocoa/src/main/prefixes.properties \
	--prefixes $(APPS_ROOT)/core-cocoa/src/main/prefixes.properties

SOURCES_DIR = Sources
BUILD_DIR = Build

EXCLUDED_FILES	= $(foreach class,$(EXCLUDED_CLASSES),$(subst .,/,$(class)).java)
JAVA_FILES = $(filter-out $(EXCLUDED_FILES),$(foreach dir,$(SOURCE_ROOTS),$(shell find $(dir) -name "*.java" | awk -F '$(dir)/' '{print $$NF}')))
OBJC_FILES = $(addprefix $(SOURCES_DIR)/, $(JAVA_FILES:.java=.m))
OBJECT_FILES = $(addprefix $(BUILD_DIR)/, $(JAVA_FILES:.java=.o))
SOURCE_PATHS = $(subst $(space),:,$(SOURCE_ROOTS))

#JAVA_FILES = $(foreach $(dir),$(SOURCE_ROOTS),$(shell find $(dir) -type f -name '*.java'))
#JAVA_FILES = $(foreach dir,$(SOURCE_ROOTS),$(shell find $(dir) -name "*.java"))

# SOURCE_PATHS = $(SOURCE_PATHS):$(APPS_ROOT)/runtime/src/main/java
# SOURCE_PATHS = $(SOURCE_PATHS):$(APPS_ROOT)/runtime/src/generic/java
#SOURCE_PATHS = $(SOURCE_PATHS):$(APPS_ROOT)/runtime-cocoa/src/main/java
#SOURCE_PATHS = $(SOURCE_PATHS):$(APPS_ROOT)/runtime/src/main/java

# Main Runtime Classes
# MAIN_DEST_DIR = Sources
# MAIN_BUILD_DIR = Build

# MAIN_SRC_DIR = ../runtime/src/main/java
# MAIN_JAVA_FILES = $(shell find $(MAIN_SRC_DIR) -type f -name '*.java')
# MAIN_JAVA_CLASSES = $(subst $(MAIN_SRC_DIR)/,, $(MAIN_JAVA_FILES))
# MAIN_OBJC_FILES = $(addprefix $(MAIN_BUILD_DIR)/, $(MAIN_JAVA_CLASSES:.java=.m))
# MAIN_OBJECTS = $(addprefix $(MAIN_BUILD_DIR)/, $(MAIN_JAVA_CLASSES:.java=.o))

# # Generic Runtime Classes
# GENERIC_BUILD_DIR = build/objc/generic
# GENERIC_SRC_DIR = ../runtime/src/generic/java
# GENERIC_JAVA_FILES = $(shell find $(GENERIC_SRC_DIR) -type f -name '*.java')
# GENERIC_JAVA_CLASSES = $(subst $(GENERIC_SRC_DIR)/,, $(GENERIC_JAVA_FILES))
# GENERIC_OBJC_FILES = $(addprefix $(GENERIC_BUILD_DIR)/, $(GENERIC_JAVA_CLASSES:.java=.m))
# GENERIC_OBJECTS = $(addprefix $(GENERIC_BUILD_DIR)/, $(GENERIC_OBJC_FILES:.java=.o))

# # Cocoa Runtime Classes
# COCOA_BUILD_DIR = build/objc/cocoa
# COCOA_SRC_DIR = src/main/java
# COCOA_JAVA_FILES = $(shell find $(COCOA_SRC_DIR) -type f -name '*.java')
# COCOA_JAVA_CLASSES = $(subst $(COCOA_SRC_DIR)/,, $(COCOA_JAVA_FILES))
# COCOA_OBJC_FILES = $(addprefix $(COCOA_BUILD_DIR)/, $(COCOA_JAVA_CLASSES:.java=.m))
# COCOA_OBJECTS = $(addprefix $(COCOA_BUILD_DIR)/, $(COCOA_OBJC_FILES:.java=.o))

# default:
# 	$(EXCLUDED_FILES)

default: $(BUILD_DIR) $(SOURCES_DIR) translate $(OBJECT_FILES)
	@echo $(JAVA_FILES)
	@ar rcs $(BUILD_DIR)/$(LIBRARY_NAME) $(OBJECT_FILES)
	@ranlib $(BUILD_DIR)/$(LIBRARY_NAME)

#	$(J2OBJCC) -o $(BUILD_DIR)/$(LIBRARY_NAME) $(OBJECT_FILES)

# Translating files

UPDATED_SOURCES = $(BUILD_DIR)/.sources.list

translate: pre_translate $(OBJC_FILES)
	@if [ `cat $(UPDATED_SOURCES) | wc -l` -ge 1 ] ; then \
	$(J2OBJC) $(J2OBJC_ARGS) $(PREFIXES) -sourcepath $(SOURCE_PATHS) -d $(SOURCES_DIR) \
	`cat $(UPDATED_SOURCES)` ; \
	fi

pre_translate:
	@rm -f $(UPDATED_SOURCES)
	@touch $(UPDATED_SOURCES)

# JAVA_SOURCES = $(TMPDIR).sources.list

# translate: pre_translate $(OBJC_SOURCES)
# 	@if [ `cat $(JAVA_SOURCES) | wc -l` -ge 1 ] ; then \
# 	  $(J2OBJC) -sourcepath $(SOURCE_DIR) -d $(BUILD_DIR) \
# 	    `cat $(JAVA_SOURCES)` ; \
# 	fi

# pre_translate: $(BUILD_DIR)
# 	@rm -f $(JAVA_SOURCES)
# 	@touch $(JAVA_SOURCES)

# $(BUILD_DIR)/%.m $(BUILD_DIR)/%.h: $(SOURCE_DIR)/%.java
# 	@echo $? >> $(JAVA_SOURCES)

define SOURCE_ROOT_RULE
$(SOURCES_DIR)/%.m: $1/%.java
	@echo $$? >> $$(UPDATED_SOURCES)
endef
$(foreach root,$(SOURCE_ROOTS),$(eval $(call SOURCE_ROOT_RULE,$(root))))

# Compile files
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

# $(SOURCES_DIR)/%.m $(SOURCES_DIR)/%.h: $(APPS_ROOT)/runtime/src/main/java/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(BUILD_DIR)/ $?

# $(SOURCES_DIR)/%.m $(SOURCES_DIR)/%.h: $(APPS_ROOT)/runtime/src/generic/java/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(BUILD_DIR)/ $?

# $(SOURCES_DIR)/%.m $(SOURCES_DIR)/%.h: $(APPS_ROOT)/runtime-cocoa/src/main/java/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(BUILD_DIR)/ $?

# $(SOURCES_DIR)/%.m $(SOURCES_DIR)/%.h: $(APPS_ROOT)/core/src/main/java/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(BUILD_DIR)/ $?

# $(SOURCES_DIR)/%.m $(SOURCES_DIR)/%.h: $(APPS_ROOT)/core-cocoa/src/main/java/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(BUILD_DIR)/ $?

# $(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(MAIN_BUILD_DIR)/ $?

# default: translate
# 	@echo $(JAVA_CLASSES)
	
# translate: $(MAIN_OBJC_FILES) $(GENERIC_OBJC_FILES) $(COCOA_OBJC_FILES)

# $(MAIN_BUILD_DIR)/%.m: $(MAIN_SRC_DIR)/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(MAIN_BUILD_DIR)/ $?

# $(GENERIC_BUILD_DIR)/%.m: $(GENERIC_SRC_DIR)/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(GENERIC_BUILD_DIR)/ $?

# $(COCOA_BUILD_DIR)/%.m: $(COCOA_SRC_DIR)/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(COCOA_BUILD_DIR)/ $?

# Main Classes
# MAIN_BUILD_DIR = build/objc/main
# MAIN_SRC_DIR = src/main/java
# MAIN_JAVA_FILES = $(shell find $(MAIN_SRC_DIR) -type f -name '*.java')
# MAIN_JAVA_CLASSES = $(subst $(MAIN_SRC_DIR)/,, $(MAIN_JAVA_FILES))
# MAIN_OBJC_FILES = $(addprefix $(MAIN_BUILD_DIR)/, $(MAIN_JAVA_CLASSES:.java=.m))
# MAIN_OBJECTS = $(addprefix $(MAIN_BUILD_DIR)/, $(MAIN_JAVA_CLASSES:.java=.o))

# Template Classes
# TEMPLATE_BUILD_DIR = build/objc/template
# TEMPLATE_SRC_DIR = src/template/java
# TEMPLATE_JAVA_FILES = $(shell find $(TEMPLATE_SRC_DIR) -type f -name '*.java')
# TEMPLATE_JAVA_CLASSES = $(subst $(TEMPLATE_SRC_DIR)/,, $(TEMPLATE_JAVA_FILES))
# TEMPLATE_OBJC_FILES = $(addprefix $(TEMPLATE_BUILD_DIR)/, $(TEMPLATE_JAVA_CLASSES:.java=.m))
# TEMPLATE_OBJECTS = $(addprefix $(TEMPLATE_BUILD_DIR)/, $(TEMPLATE_OBJC_FILES:.java=.o))

# default: translate
# 	@echo $(JAVA_CLASSES)

# translate: $(MAIN_OBJC_FILES) $(TEMPLATE_OBJC_FILES)

# $(MAIN_BUILD_DIR)/%.m: $(MAIN_SRC_DIR)/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(MAIN_BUILD_DIR)/ $?

# $(TEMPLATE_BUILD_DIR)/%.m: $(TEMPLATE_SRC_DIR)/%.java
# 	$(J2OBJC) $(J2OBJC_ARGS) -sourcepath $(SOURCE_PATHS) -d $(TEMPLATE_BUILD_DIR)/ $?	

# $(BUILD_DIR)/%.o: $(BUILD_DIR)/%.m
# $(J2OBJCC) -I$(BUILD_DIR) -c $? -o $@

# Compile all
# $(J2OBJCC) -o $(RESULT) $(OBJC_FILES)

#$(SOURCE_PATH)/%.java:
# 	@echo $@

# $(J2OBJC) -sourcepath $(SOURCE_PATH) -d $(BUILD_DIR) $(SOURCE_PATH)/$@
# @echo $@
# $(J2OBJC) -sourcepath $(SOURCE_PATH) -d $(BUILD_DIR) $?

# translate: $(BUILD_DIR) $(OBJC_FILES)

# default: translate $(OBJC_FILES)
#	$(J2OBJC) -o $(RESULT) $(OBJECTS)
#
# translate: $(BUILD_DIR) $(OBJC_SOURCES)
#
# clean:
#	@rm -rf $(RESULT) $(BUILD_DIR)
#
# $(BUILD_DIR)/%.m $(BUILD_DIR)/%.h: $(SOURCE_DIR)/%.java
#	$(J2OBJC) -sourcepath $(SOURCE_DIR) -d $(BUILD_DIR) $?
#
# $(BUILD_DIR)/%.o: $(BUILD_DIR)/%.m
#	$(J2OBJCC) -I$(BUILD_DIR) -c $? -o $@
#
# $(BUILD_DIR):
#	@mkdir $(BUILD_DIR)