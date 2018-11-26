package com.clevertap.android.sdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

class CTInboxController {
    private boolean initialized;
    private int count;
    private int unreadCount;
    private HashMap messages,unreadMessages;
    private String accountId;
    private String guid;
    private String userId;
    CTNotificationInboxListener listener;
    private CTUserDAO userDAO;
    private DBAdapter dbAdapter;

    private CTInboxController(String accountId, String guid, DBAdapter adapter){
        this.accountId = accountId;
        this.guid = guid;
        this.initialized = true;
        this.userId = this.accountId + this.guid;
        this.dbAdapter = adapter;
        this.userDAO = new CTUserDAO(this.accountId,this.guid,this.userId);
        int returnCode = this.dbAdapter.storeInboxUser(this.userDAO);
    }

    static CTInboxController initWithAccountId(String accountId, String guid, DBAdapter adapter){
        try{
            return new CTInboxController(accountId,guid, adapter);
        }catch (Throwable t){
            return null;
        }
    }

    void updateMessages(JSONArray inboxMessages){
        if(!this.isInitialized()) return;

        boolean haveUpdates = updateUserMessages(inboxMessages);
        if(haveUpdates){
            notifyUpdate();
        }
    }

    void deleteMessageWithId(String messageId){
        CTMessageDAO messageDAO = getMessageDaoForId(messageId);
        if(messageDAO!=null){
            boolean deletedMessage =  this.dbAdapter.deleteMessageForId(messageId);
            if(deletedMessage) {
                notifyUpdate();
            }
        }
    }

    void markReadForMessageWithId(String messageId){
        CTMessageDAO messageDAO = getMessageDaoForId(messageId);
        if(messageDAO != null){
            boolean marked = this.dbAdapter.markReadMessageForId(messageId);
            if(marked){
                notifyUpdate();
            }
        }
    }

    JSONObject getMessageForId(String messageId){
        if(this.isInitialized()) {
            CTMessageDAO messageDAO = this.dbAdapter.getMessageForId(messageId);
            return messageDAO.toJSON();
        }
        else {
            return null;
        }
    }

    private CTMessageDAO getMessageDaoForId(String messageId){
        return this.dbAdapter.getMessageForId(messageId);
    }

    int count(){
        if(this.isInitialized()){
            return userDAO.getNewMessages().length();
        }else{
            return -1;
        }
    }

    int unreadCount(){
        if(this.isInitialized()){
            return this.dbAdapter.getUnreadCount();
        }else{
            return -1;
        }
    }

    ArrayList<CTMessageDAO> getMessages(){
        if(this.isInitialized()){
            return this.dbAdapter.getMessages();
        }else{
            return null;
        }
    }

    ArrayList<CTMessageDAO> getUnreadMessages(){
        if(this.isInitialized()){
            return this.dbAdapter.getUnreadMessages();
        }else{
            return null;
        }
    }

    boolean isInitialized() {
        return initialized;
    }

    public int getCount() {
        return count;
    }

    public int getUnreadCount() {
        return unreadCount;
    }


    String getAccountId() {
        return accountId;
    }

    String getGuid() {
        return guid;
    }

    String getUserId() {
        return userId;
    }

    CTNotificationInboxListener getListener() {
        return listener;
    }

    private void notifyUpdate(){
        if(listener!=null){
            listener.inboxMessagesDidUpdate();
        }
    }

    private void notifyInitialized(){
        if(listener!=null){
            listener.inboxDidInitialize();
        }
    }

    private boolean updateUserMessages(JSONArray inboxMessages){
        userDAO.setNewMessages(inboxMessages);
        boolean haveUpdates = false;
        for(int i=0;i<inboxMessages.length();i++){
            try {
                JSONObject inboxMessage = inboxMessages.getJSONObject(i);
                if(!inboxMessage.has("id")){
                    Logger.d("Notification inbox message doesn't have proper JSON");
                    return false;
                }

                //TODO Duplicating logic

                CTMessageDAO messageDAO = CTMessageDAO.initWithJSON(inboxMessage, userDAO.getUserId());
                ArrayList<CTMessageDAO> messageDAOArrayList = new ArrayList<>();
                if(messageDAO!=null) {
                    messageDAOArrayList.add(messageDAO);
                }
                if(messageDAOArrayList.size()>0){
                    this.dbAdapter.storeMessagesForUser(messageDAOArrayList);
                    haveUpdates = true;
                }
            }catch (JSONException e){
                Logger.d("Unable to update notification inbox messages - "+e.getLocalizedMessage());
            }
        }
        return haveUpdates;
    }
}
