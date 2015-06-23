package im.actor.model.js.providers.locale;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ResourcePrototype;

public class LocaleBundle_default_InlineClientBundleGenerator implements im.actor.model.js.providers.locale.LocaleBundle {
  private static LocaleBundle_default_InlineClientBundleGenerator _instance0 = new LocaleBundle_default_InlineClientBundleGenerator();
  private void AppTextInitializer() {
    AppText = new com.google.gwt.resources.client.TextResource() {
      // file:/Users/ex3ndr/Develop/actor-platform/actor-apps/library/core-js/build/resources/main/AppText.properties
      public String getText() {
        return "TimeShortYesterday=Yest\nTimeShortNow=Now\nTimeShortMinutes={minutes} min\nTimeShortHours={hours} hrs\n\nOnlineOn=online\nOnlineOff=offline\n\nOnlineNow=last seen just now\nOnlineLastSeenToday=last seen today at {time}\nOnlineLastSeenYesterday=last seen yesterday at {time}\nOnlineLastSeenDate=last seen {date}\nOnlineLastSeenDateTime=last seen {date} at {time}\n\nGroupOnline={count} online\nGroupMembers={count} members\n\nTyping=typing...\nTypingUser={user} is typing...\nTypingMultiple = {count} people are typing...\n\nFileB={bytes} B\nFileKb={kbytes} KB\nFileMb={mbytes} MB\nFileGb={gbytes} GB\n\nYou=You\nThee=You\n\nContentUnsupported=Unsupported content\nContentDocument=Document\nContentPhoto=Photo\nContentVideo=Video\n\nServiceRegistered=Joined Actor\nServiceRegisteredFull={name} joined Actor Network\nServiceNewDevice=Added new device\nServiceNewDeviceFull={name} added new device to it's account\nServiceGroupCreated={name} created the group\nServiceGroupCreatedFull={name} created the group\nServiceGroupLeaved={name} left group\nServiceGroupJoined={name} joined group\nServiceGroupAdded={name} invited {name_added}\nServiceGroupKicked={name} kicked {name_kicked}\nServiceGroupTitle={name} changed the group name\nServiceGroupTitleFull={name} changed the group name to \"{title}\"\nServiceGroupAvatarChanged={name} changed the group photo\nServiceGroupAvatarRemoved={name} removed the group photo";
      }
      public String getName() {
        return "AppText";
      }
    }
    ;
  }
  private static class AppTextInitializer {
    static {
      _instance0.AppTextInitializer();
    }
    static com.google.gwt.resources.client.TextResource get() {
      return AppText;
    }
  }
  public com.google.gwt.resources.client.TextResource AppText() {
    return AppTextInitializer.get();
  }
  private void MonthsInitializer() {
    Months = new com.google.gwt.resources.client.TextResource() {
      // file:/Users/ex3ndr/Develop/actor-platform/actor-apps/library/core-js/build/resources/main/Months.properties
      public String getText() {
        return "JanShort=Jan\nFebShort=Feb\nMarShort=Mar\nAprShort=Apr\nMayShort=May\nJunShort=Jun\nJulShort=Jul\nAugShort=Aug\nSepShort=Sep\nOctShort=Oct\nNovShort=Nov\nDecShort=Dec\n\nJanFull=January\nFebFull=February\nMarFull=March\nAprFull=April\nMayFull=May\nJunFull=June\nJulFull=July\nAugFull=August\nSepFull=September\nOctFull=October\nNovFull=November\nDecFull=December";
      }
      public String getName() {
        return "Months";
      }
    }
    ;
  }
  private static class MonthsInitializer {
    static {
      _instance0.MonthsInitializer();
    }
    static com.google.gwt.resources.client.TextResource get() {
      return Months;
    }
  }
  public com.google.gwt.resources.client.TextResource Months() {
    return MonthsInitializer.get();
  }
  private static java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype> resourceMap;
  private static com.google.gwt.resources.client.TextResource AppText;
  private static com.google.gwt.resources.client.TextResource Months;
  
  public ResourcePrototype[] getResources() {
    return new ResourcePrototype[] {
      AppText(), 
      Months(), 
    };
  }
  public ResourcePrototype getResource(String name) {
    if (GWT.isScript()) {
      return getResourceNative(name);
    } else {
      if (resourceMap == null) {
        resourceMap = new java.util.HashMap<java.lang.String, com.google.gwt.resources.client.ResourcePrototype>();
        resourceMap.put("AppText", AppText());
        resourceMap.put("Months", Months());
      }
      return resourceMap.get(name);
    }
  }
  private native ResourcePrototype getResourceNative(String name) /*-{
    switch (name) {
      case 'AppText': return this.@im.actor.model.js.providers.locale.LocaleBundle::AppText()();
      case 'Months': return this.@im.actor.model.js.providers.locale.LocaleBundle::Months()();
    }
    return null;
  }-*/;
}
