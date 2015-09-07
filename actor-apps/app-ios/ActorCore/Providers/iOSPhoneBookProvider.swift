//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class PhoneBookProvider: NSObject, ACPhoneBookProvider {
    
    func loadPhoneBookWithCallback(callback: ACPhoneBookProvider_Callback!) {
        var rawBook = ABAddressBookCreateWithOptions(nil, nil);
        if (rawBook == nil) {
            println("Access to AddressBook denied");
            callback.onLoadedWithContacts(JavaUtilArrayList())
            return
        }
        var book: ABAddressBook = rawBook.takeRetainedValue()
        ABAddressBookRequestAccessWithCompletion(book, { (granted: Bool, error: CFError!) -> Void in
            if (!granted) {
                println("Access to AddressBook denied");
                callback.onLoadedWithContacts(JavaUtilArrayList())
                return;
            }
            
            let numbersSet = NSCharacterSet(charactersInString: "0123456789").invertedSet
            var contacts = JavaUtilArrayList()
            var index = 1
            if let people = ABAddressBookCopyArrayOfAllPeople(book).takeRetainedValue() as? [ABRecordRef] {
                for person in people {
                    var firstName = self.extractString(person as ABRecord, propertyName: kABPersonFirstNameProperty)
                    var middleName = self.extractString(person as ABRecord, propertyName: kABPersonMiddleNameProperty)
                    var lastName = self.extractString(person as ABRecord, propertyName: kABPersonLastNameProperty)
                    
                    var contactName :String? = " ".join([firstName, middleName, lastName]
                        .filter({ (val: String?) -> Bool in return val != nil && val!.size > 0})
                        .map({ (val: String?) -> String in return val! }))
                    if (firstName == "Name not specified") {
                        contactName = nil
                    }
                    
                    var contactPhones = JavaUtilArrayList()
                    var contactEmails = JavaUtilArrayList()
                    var contact = ACPhoneBookContact(long: jlong(index++), withNSString: contactName, withJavaUtilArrayList: contactPhones, withJavaUtilArrayList: contactEmails)
                    
                    if let phones: ABMultiValueRef =
                        self.extractProperty(person as ABRecord, propertyName: kABPersonPhoneProperty) as ABMultiValueRef? {
                            for i in 0...ABMultiValueGetCount(phones) {
                                var phoneStr = self.extractString(phones, index: i)
                                if (phoneStr == nil || phoneStr!.trim().size == 0) {
                                    continue
                                }
                                phoneStr = phoneStr?.strip(numbersSet)
                                var phoneVal = phoneStr?.toLong()
                                if (phoneVal != nil) {
                                    contactPhones.addWithId(ACPhoneBookPhone(long: jlong(index++), withLong: jlong(phoneVal!)))
                                }
                            }
                    }
                    
                    if let emails: ABMultiValueRef =
                        self.extractProperty(person as ABRecord, propertyName: kABPersonEmailProperty) as ABMultiValueRef? {
                            for i in 0...ABMultiValueGetCount(emails) {
                                var emailStr = self.extractString(emails, index: i)
                                if (emailStr == nil || emailStr!.trim().size == 0) {
                                    continue
                                }
                                contactEmails.addWithId(ACPhoneBookEmail(long: jlong(index++), withNSString: emailStr!))
                            }
                    }
                    
                    if (contactPhones.size() != 0 || contactEmails.size() != 0) {
                        contacts.addWithId(contact)
                    }
                }
            }
            
            callback.onLoadedWithContacts(contacts)
        });

    }
    
    private func extractString(record: ABRecord, propertyName : ABPropertyID) -> String? {
        return extractProperty(record, propertyName: propertyName)
    }
    
    private func extractProperty<T>(record: ABRecord, propertyName : ABPropertyID) -> T? {
        //the following is two-lines of code for a reason. Do not combine (compiler optimization problems)
        var value: AnyObject? = ABRecordCopyValue(record, propertyName)?.takeRetainedValue()
        return value as? T
    }
    
    private func extractString(record: ABMultiValueRef, index: Int) -> String? {
        var value: AnyObject? = ABMultiValueCopyValueAtIndex(record, index)?.takeRetainedValue()
        return value as? String
    }
}