package com.service.service;

import com.service.constants.values.TwilioData;
import com.service.interfaces.MessageSender;
import com.service.model.MessageSenderRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class TwilioMessageSenderService implements MessageSender {

    @Override
    public void sendMessage(MessageSenderRequest request) {
        try{
            Twilio.init(TwilioData.ACCOUNT_SID, TwilioData.AUTH_TOKE);
            Map<String,String> otpMap = new HashMap<>();
            Random random = new Random();
            String otp = String.format("%04d", random.nextInt(10000));
            Message message = Message.creator(new PhoneNumber("7759856927"), new PhoneNumber(TwilioData.TWILIO_PHONE), otp)
                    .create();
            System.out.println("here is my id:"+message.getSid());// Unique resource ID created to manage this transaction
            System.out.println("OTP SENT ===>");
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public Integer getOtp(String phoneNumber) {
        try{
            Twilio.init(TwilioData.ACCOUNT_SID, TwilioData.AUTH_TOKE);
            Map<String,String> otpMap = new HashMap<>();
            Random random = new Random();
            String otp = String.format("%04d", random.nextInt(10000));
            String messageString = "OneStore OTP code is "+otp;
            phoneNumber = "+91"+phoneNumber;
            Message message = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(TwilioData.TWILIO_PHONE), messageString)
                    .create();
            System.out.println("here is my id:"+message.getSid());// Unique resource ID created to manage this transaction
            return Integer.valueOf(otp);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
