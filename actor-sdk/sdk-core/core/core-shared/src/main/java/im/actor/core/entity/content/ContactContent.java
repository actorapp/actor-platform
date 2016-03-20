/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.core.api.ApiJsonMessage;
import im.actor.core.entity.Contact;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.json.JSONArray;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public class ContactContent extends AbsContent {

    private String name;
    private String photo64;
    private String rawJson;
    private ArrayList<String> phones;
    private ArrayList<String> emails;

    @NotNull
    public static ContactContent create(@NotNull String name, @NotNull ArrayList<String> phones, @NotNull ArrayList<String> emails, @Nullable String base64photo) {

        try {
            JSONObject obj = new JSONObject();
            obj.put("dataType", "contact");
            JSONObject contact = new JSONObject();
            contact.put("name", name);
            if (base64photo != null) {
                contact.put("photo", base64photo);
            }
            JSONArray phoneArray = new JSONArray();
            for (String phone : phones) {
                phoneArray.put(phone);
            }
            JSONArray emailArray = new JSONArray();
            for (String phone : emails) {
                emailArray.put(phone);
            }
            contact.put("emails", emailArray);
            contact.put("phones", phoneArray);
            JSONObject data = new JSONObject();
            data.put("contact", contact);
            obj.put("data", data);

            return new ContactContent(new ContentRemoteContainer(new ApiJsonMessage(obj.toString())));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public ContactContent(ContentRemoteContainer contentContainer) throws JSONException {
        super(contentContainer);

        rawJson = ((ApiJsonMessage) contentContainer.getMessage()).getRawJson();
        JSONObject data = new JSONObject(rawJson).getJSONObject("data");
        JSONObject contact = data.getJSONObject("contact");
        name = contact.getString("name");
        try {
            photo64 = contact.getString("photo");
        } catch (Exception e) {
//            e.printStackTrace();
        }
        phones = new ArrayList<String>();
        JSONArray phonesJson = contact.getJSONArray("phones");
        for (int i = 0; i < phonesJson.length(); i++) {
            phones.add(phonesJson.getString(i));
        }
        emails = new ArrayList<String>();
        JSONArray emailsJson = contact.getJSONArray("emails");
        for (int i = 0; i < emailsJson.length(); i++) {
            emails.add(emailsJson.getString(i));
        }
    }

    public ArrayList<String> getPhones() {
        return phones;
    }

    public String getPhoto64() {
        return photo64;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public String getName() {
        return name;
    }

    public String getRawJson() {
        return rawJson;
    }
}
