package com.example.networktools;

import java.util.BitSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNSPacket {
    private final String domainRegex = "^(((?!-))(xn--|_)?[a-z0-9-]{0,61}[a-z0-9]{1,1}\\.)*(xn--)?([a-z0-9][a-z0-9\\-]{0,60}|[a-z0-9-]{1,30}\\.[a-z]{2,})$";
    private final int noOfFields = 16;
    private final PacketField[] dnsPacketFields = new PacketField[noOfFields];
    private final String[] fieldNames = new String[]{"id", "qr", "opCode", "authoritativeAnswer", "truncated", "recursionDesired", "recursionAvailable", "z", "replyCode", "questionCount", "answerCount", "authoritativeRecordsCount", "additionalRecordsCount", "domainName", "queryType", "queryClass"};
    private final int[] fieldLengths = new int[]{16, 1, 4, 1, 1, 1, 1, 3, 4, 16, 16, 16, 16, 0, 16, 16};
    private final int[] defaultValues;
    private final int domainIndex = 13;
    private boolean isDomainSet = false;
    private BitSet finalBitSet;

    public DNSPacket() {
        defaultValues = new int[]{generateRandom16Bit(),0,0,0,0,1,0,0,0,1,0,0,0,-1,1,1};
        for (int i = 0; i < noOfFields; i++) {
            if (fieldLengths[i] == 0) {
                dnsPacketFields[i] = new PacketField(fieldNames[i]);
            } else {
                dnsPacketFields[i] = new PacketField(fieldLengths[i], fieldNames[i],defaultValues[i]);
            }
        }
    }

    public byte[] getPacketBytes() throws IllegalStateException{
        if(!this.isDomainSet){
            throw new IllegalStateException("Domain name is not set yet!");
        }
        finalBitSet = new BitSet();
        int index =0;
        for(int i=0;i<dnsPacketFields.length;i++){
            int n = dnsPacketFields[i].nbits;
            for(int j=0;j<n;j++){
                finalBitSet.set(index+j,dnsPacketFields[i].bits.get(j));
            }
            index += n;
        }
        return finalBitSet.toByteArray();
    }

    public void setDomain(String domain) throws IllegalArgumentException{
        Pattern macPattern = Pattern.compile(this.domainRegex);
        Matcher matcher = macPattern.matcher(domain);
        if(!matcher.find()){
            throw new IllegalArgumentException("Invalid domain name");
        }
        this.dnsPacketFields[this.domainIndex].setValue(domain);
        this.isDomainSet = true;
    }

//    private void setDefaults(){
//        defaultValues.put("id",generateRandom16Bit());
//        defaultValues.put("qr",0);
//        defaultValues.put("opCode",0);
//        defaultValues.put("authoritativeAnswer",0);
//        defaultValues.put("truncated",0);
//        defaultValues.put("recursionDesired",1);
//        defaultValues.put("recursionAvailable",0);
//        defaultValues.put("z",0);
//        defaultValues.put("replyCode",0);
//        defaultValues.put("questionCount",1);
//        defaultValues.put("answerCount",0);
//        defaultValues.put("authoritativeRecordsCount",0);
//        defaultValues.put("additionalRecordsCount",0);
//        defaultValues.put("queryType",1);
//        defaultValues.put("queryClass",1);
//    }

    private int generateRandom16Bit(){
        Random r = new Random();
        return r.nextInt(1<<16);
    }
}
