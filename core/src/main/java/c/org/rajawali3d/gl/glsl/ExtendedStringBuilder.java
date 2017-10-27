package c.org.rajawali3d.gl.glsl;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ExtendedStringBuilder {

    @NonNull
    private final StringBuilder builder;

    private int tabCount = 0;

    ExtendedStringBuilder(@NonNull StringBuilder builder) {
        this.builder = builder;
    }

    public void increaseTabCount() {
        ++tabCount;
    }

    public void decreaseTabCount() {
        --tabCount;
        if (tabCount < 0) {
            tabCount = 0;
        }
    }

    public void setTabCount(@IntRange(from = 0) int count) {
        tabCount = count;
        if (tabCount < 0) {
            tabCount = 0;
        }
    }

    public void appendTabs() {
        int i = tabCount;
        while (i > 0) {
            builder.append('\t');
            --i;
        }
    }

    public ExtendedStringBuilder append(Object obj) {
        builder.append(obj);
        return this;
    }

    public ExtendedStringBuilder append(String str) {
        builder.append(str);
        return this;
    }

    public ExtendedStringBuilder append(StringBuffer sb) {
        builder.append(sb);
        return this;
    }

    public ExtendedStringBuilder append(CharSequence s) {
        builder.append(s);
        return this;
    }

    public ExtendedStringBuilder append(CharSequence s, int start, int end) {
        builder.append(s, start, end);
        return this;
    }

    public ExtendedStringBuilder append(char[] str) {
        appendTabs();
        builder.append(str);
        return this;
    }

    public ExtendedStringBuilder append(char[] str, int offset, int len) {
        builder.append(str, offset, len);
        return this;
    }

    public ExtendedStringBuilder append(boolean b) {
        builder.append(b);
        return this;
    }

    public ExtendedStringBuilder append(char c) {
        builder.append(c);
        return this;
    }

    public ExtendedStringBuilder append(int i) {
        builder.append(i);
        return this;
    }

    public ExtendedStringBuilder append(long lng) {
        builder.append(lng);
        return this;
    }

    public ExtendedStringBuilder append(float f) {
        builder.append(f);
        return this;
    }

    public ExtendedStringBuilder append(double d) {
        appendTabs();
        builder.append(d);
        return this;
    }

    public ExtendedStringBuilder appendCodePoint(int codePoint) {
        builder.appendCodePoint(codePoint);
        return this;
    }

    public ExtendedStringBuilder delete(int start, int end) {
        builder.delete(start, end);
        return this;
    }

    public ExtendedStringBuilder deleteCharAt(int index) {
        builder.deleteCharAt(index);
        return this;
    }

    public ExtendedStringBuilder replace(int start, int end, String str) {
        builder.replace(start, end, str);
        return this;
    }

    public ExtendedStringBuilder insert(int index, char[] str, int offset, int len) {
        builder.insert(index, str, offset, len);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, Object obj) {
        builder.insert(offset, obj);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, String str) {
        builder.insert(offset, str);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, char[] str) {
        builder.insert(offset, str);
        return this;
    }

    public ExtendedStringBuilder insert(int dstOffset, CharSequence s) {
        builder.insert(dstOffset, s);
        return this;
    }

    public ExtendedStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        builder.insert(dstOffset, s, start, end);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, boolean b) {
        builder.insert(offset, b);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, char c) {
        builder.insert(offset, c);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, int i) {
        builder.insert(offset, i);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, long l) {
        builder.insert(offset, l);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, float f) {
        builder.insert(offset, f);
        return this;
    }

    public ExtendedStringBuilder insert(int offset, double d) {
        builder.insert(offset, d);
        return this;
    }

    public int indexOf(String str) {
        return builder.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return builder.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return builder.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return builder.lastIndexOf(str, fromIndex);
    }

    public ExtendedStringBuilder reverse() {
        builder.reverse();
        return this;
    }

    public String toString() {
        return builder.toString();
    }

    public int length() {
        return builder.length();
    }

    public int capacity() {
        return builder.capacity();
    }

    public void ensureCapacity(int minimumCapacity) {
        builder.ensureCapacity(minimumCapacity);
    }

    public void trimToSize() {
        builder.trimToSize();
    }

    public void setLength(int newLength) {
        builder.setLength(newLength);
    }

    public char charAt(int index) {
        return builder.charAt(index);
    }

    public int codePointAt(int index) {
        return builder.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return builder.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return builder.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return builder.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        builder.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void setCharAt(int index, char ch) {
        builder.setCharAt(index, ch);
    }

    public String substring(int start) {
        return builder.substring(start);
    }

    public CharSequence subSequence(int start, int end) {
        return builder.subSequence(start, end);
    }

    public String substring(int start, int end) {
        return builder.substring(start, end);
    }
}
