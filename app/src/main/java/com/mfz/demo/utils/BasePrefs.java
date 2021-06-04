package com.mfz.demo.utils;

import android.text.TextUtils;

import com.mfz.prefsbuilder.BasePrefsClass;
import com.mfz.prefsbuilder.BasePrefsInterface;

import net.grandcentrix.tray.TrayPreferences;
import net.grandcentrix.tray.core.OnTrayPreferenceChangeListener;

/**
 * @author mz
 */
@BasePrefsClass
public class BasePrefs implements BasePrefsInterface {

    private final TrayPreferences mPreferences;

    public BasePrefs(String name) {
        mPreferences = new TrayPreferences(Utils.getContext(), name, 1);
    }

    public void registerOnChangeListener(OnTrayPreferenceChangeListener listener) {
        try {
            mPreferences.registerOnTrayPreferenceChangeListener(listener);
        } catch (Exception ignore) {
        }
    }

    public void unregisterOnChangeListener(OnTrayPreferenceChangeListener listener) {
        try {
            mPreferences.unregisterOnTrayPreferenceChangeListener(listener);
        } catch (Exception ignore) {
        }
    }

    @Override
    public boolean remove(String key) {
        return mPreferences.remove(key);
    }

    @Override
    public boolean contains(final String key) {
        return mPreferences.contains(key);
    }

    @Override
    public boolean getBool(String key, boolean defVal) {
        try {
            return mPreferences.getBoolean(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setBool(String key, boolean val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public int getInt(String key, int defVal) {
        try {
            return mPreferences.getInt(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setInt(String key, int val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public byte getByte(String key, byte defVal) {
        try {
            return (byte) mPreferences.getInt(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setByte(String key, byte val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public char getChar(String key, char defVal) {
        try {
            return (char) mPreferences.getInt(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setChar(String key, char val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public short getShort(String key, short defVal) {
        try {
            return (short) mPreferences.getInt(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setShort(String key, short val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public long getLong(String key, long defVal) {
        try {
            return mPreferences.getLong(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setLong(String key, long val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public float getFloat(String key, float defVal) {
        try {
            return mPreferences.getFloat(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setFloat(String key, float val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public String getString(String key, String defVal) {
        try {
            return mPreferences.getString(key, defVal);
        } catch (Exception ignore) {
        }
        return defVal;
    }

    @Override
    public void setString(String key, String val) {
        try {
            mPreferences.put(key, val);
        } catch (Exception ignore) {
        }
    }

    @Override
    public double getDouble(String key, double defVal) {
        String string = getString(key, null);
        if (TextUtils.isEmpty(string)) {
            return defVal;
        }
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defVal;
    }

    @Override
    public void setDouble(String key, double val) {
        setString(key, String.valueOf(val));
    }

}
