package im.actor.images.cache;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by ex3ndr on 13.09.14.
 */
public abstract class BitmapClasificator {

    public abstract int getType(int w, int h);

    public abstract int getType(Bitmap bitmap);

    public abstract CacheConfig[] getConfigs();


    public static class Builder {
        private ArrayList<Rule> rules = new ArrayList<Rule>();

        private Rule currentRule;

        public Builder startAny() {
            currentRule = new Rule();
            currentRule.filter = new Filter() {
                @Override
                public boolean checkType(int w, int h) {
                    return true;
                }
            };
            return this;
        }

        public Builder startExactSize(final int w, final int h) {
            currentRule = new Rule();
            currentRule.filter = new Filter() {
                @Override
                public boolean checkType(int w2, int h2) {
                    return w2 == w && h2 == h;
                }
            };
            return this;
        }

        public Builder startLessOrEqSize(final int w, final int h) {
            currentRule = new Rule();
            currentRule.filter = new Filter() {
                @Override
                public boolean checkType(int w2, int h2) {
                    return w2 <= w && h2 <= h;
                }
            };
            return this;
        }

        public Builder startLargeOrEqSize(final int w, final int h) {
            currentRule = new Rule();
            currentRule.filter = new Filter() {
                @Override
                public boolean checkType(int w2, int h2) {
                    return w2 >= w && h2 >= h;
                }
            };
            return this;
        }

        public Builder useSizeInBytes() {
            currentRule.useSizeInBytes = true;
            return this;
        }

        public Builder useSizeInAmount() {
            currentRule.useSizeInBytes = false;
            return this;
        }

        public Builder startFilter(Filter filter) {
            currentRule = new Rule();
            currentRule.filter = filter;
            return this;
        }

        public Builder setLruSize(int size) {
            currentRule.lruSize = size;
            return this;
        }

        public Builder setFreeSize(int size) {
            currentRule.freeSize = size;
            return this;
        }

        public Builder endFilter() {
            rules.add(currentRule);
            currentRule = null;
            return this;
        }

        public BitmapClasificator build() {
            final CacheConfig[] configs = new CacheConfig[rules.size()];
            final Filter[] fiters = new Filter[rules.size()];
            for (int i = 0; i < rules.size(); i++) {
                Rule rule = rules.get(i);
                configs[i] = new CacheConfig(i, rule.lruSize != 0, rule.freeSize != 0, rule.useSizeInBytes, rule.lruSize, rule.freeSize);
                fiters[i] = rule.filter;
            }

            return new BitmapClasificator() {

                @Override
                public int getType(int w, int h) {
                    for (int i = 0; i < fiters.length; i++) {
                        if (fiters[i].checkType(w, h)) {
                            return i;
                        }
                    }
                    return -1;
                }

                @Override
                public int getType(Bitmap bitmap) {
                    return getType(bitmap.getWidth(), bitmap.getHeight());
                }

                @Override
                public CacheConfig[] getConfigs() {
                    return configs;
                }
            };
        }

        private class Rule {
            private Filter filter;
            private boolean useSizeInBytes;
            private int lruSize;
            private int freeSize;
        }
    }

    public static interface Filter {
        public boolean checkType(int w, int h);
    }

    /**
     * Created by ex3ndr on 13.09.14.
     */
    public static class CacheConfig {
        private int category;
        private boolean lruEnabled;
        private boolean freeEnabled;
        private boolean useSizeInBytes;
        private int maxLruSize;
        private int maxFreeSize;

        public CacheConfig(int category, boolean lruEnabled, boolean freeEnabled, boolean useSizeInBytes,
                           int maxLruSize, int maxFreeSize) {
            this.category = category;
            this.lruEnabled = lruEnabled;
            this.freeEnabled = freeEnabled;
            this.useSizeInBytes = useSizeInBytes;
            this.maxLruSize = maxLruSize;
            this.maxFreeSize = maxFreeSize;
        }

        public int getCategory() {
            return category;
        }

        public boolean isLruEnabled() {
            return lruEnabled;
        }

        public boolean isFreeEnabled() {
            return freeEnabled;
        }

        public int getMaxLruSize() {
            return maxLruSize;
        }

        public int getMaxFreeSize() {
            return maxFreeSize;
        }

        public boolean isUseSizeInBytes() {
            return useSizeInBytes;
        }
    }
}
