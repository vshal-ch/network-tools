package com.example.networktools;

import java.util.BitSet;

public class PacketField {
    BitSet bits;
    int nbits;
    int defaultValue;
    String valueStr;
    int valueInt;
    String name;
    String type;

    PacketField(String name) {
        this.name = name;
        this.type = "string";
    }

    PacketField(int nbits, String name, int defaultValue) {
        this.nbits = nbits;
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = "int";
        setValueInt(this.defaultValue);
    }

    public void setValueInt(int value) {
        this.valueInt = value;
        this.bits = getBits(this.valueInt, this.nbits);
    }

    public void setValue(String value) {
        this.valueStr = value;
        this.bits = getBitsFromStr(this.valueStr);
        this.nbits = this.bits.length() - 1;
    }

    private BitSet getBits(int defaultValue, int length) {
        BitSet result = new BitSet(length);
        int index = 0;
        while (defaultValue > 0) {
            if (defaultValue % 2 != 0) {
                result.set(index);
            }
            index++;
            defaultValue = defaultValue >> 1;
        }
        return result;
    }

    private BitSet getBitsFromStr(String value) {
        int size = 0;
        String[] parts = value.split("\\.");
        BitSet[] labelBits = new BitSet[parts.length];
        BitSet[][] partBits = new BitSet[parts.length][];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int length = part.length();
            labelBits[i] = getBits(length, 8);
            partBits[i] = new BitSet[length];
            size += 8;
            for (int j = 0; j < length; j++) {
                partBits[i][j] = getBits((int) part.charAt(j), 8);
                size += 8;
            }
        }

        size += 8;

        BitSet result = new BitSet(size);
        int index = 0;

        for (int i = 0; i < labelBits.length; i++) {
            for (int j = 0; j < 8; j++) {
                result.set(index + j, labelBits[i].get(j));
            }
            index += 8;
            for (int j = 0; j < partBits[i].length; j++) {
                for (int k = 0; k < 8; k++) {
                    result.set(index + k, partBits[i][j].get(k));
                }
                index += 8;
            }
        }

        result.set(index + 8);
        return result;
    }

}
