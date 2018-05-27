package ru.mail.colloquium.service.ab;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ru.mail.colloquium.diagnostics.DebugUtils;
import ru.mail.colloquium.model.AppData;
import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.model.entities.ContactPhoneLink;
import ru.mail.colloquium.model.types.ContactPhoneNumber;

import static ru.mail.colloquium.App.app;
import static ru.mail.colloquium.App.data;
import static ru.mail.colloquium.diagnostics.Logger.trace;
import static ru.mail.colloquium.toolkit.collections.Query.query;

public class AddressBookSyncHelper {

    public static void doSync(Context context) {
        try (AbReader abReader = new AbReader(context)) {
            AppData storage = data();
            List<Contact> contacts = storage.contacts.selectAll().toList();
            LongSparseArray<Contact> contactsByAbId = query(contacts).toLongSparseArray(c -> c.abContactId);

            List<ContactPhoneNumber> phoneNumbers = storage.phoneNumbers.selectToSync().toList();
            HashMap<String, ContactPhoneNumber> phonesByNumber = query(phoneNumbers).toMap(p -> p.normalized);
            LongSparseArray<ArrayList<ContactPhoneNumber>> phonesByContacts = query(phoneNumbers).groupByLong(p -> p.link.contact);
            LongSparseArray<ContactPhoneNumber> phonesByAbId = query(phoneNumbers).toLongSparseArray(p -> p.link.abPhoneId);


            try (AppData.Transaction transaction = storage.beginTransaction()) {
                for (SyncUnit abContact : abReader) {
                    int idxContact = contactsByAbId.indexOfKey(abContact.contact.abContactId);
                    if (idxContact < 0) {
                        // новый контакт
                        if (!abContact.phones.isEmpty()) {
                            Contact dbContact = abContact.contact;
                            dbContact.onUpdateName();
                            storage.contacts.save(dbContact);
                            for (ContactPhoneNumber abPhone : abContact.phones) {
                                int idxPhone = phonesByAbId.indexOfKey(abPhone.link.abPhoneId);
                                ContactPhoneNumber dbPhone;
                                if (idxPhone < 0) {
                                    dbPhone = phonesByNumber.get(abPhone.normalized);
                                    if (dbPhone == null) {
                                        dbPhone = abPhone.clone();
                                        storage.phoneNumbers.save(dbPhone);
                                        phonesByNumber.put(dbPhone.normalized, dbPhone);
                                        phonesByAbId.put(dbPhone.link.abPhoneId, dbPhone);
                                    }
                                } else {
                                    dbPhone = phonesByAbId.valueAt(idxPhone);
                                }
                                dbPhone.link = abPhone.link;
                                dbPhone.link.phone = dbPhone._id;
                                dbPhone.link.contact = dbContact._id;
                                storage.contactsPhonesLinks.save(dbPhone.link);
                            }
                        }
                    } else {
                        // существующий контакт
                        Contact dbContact = contactsByAbId.valueAt(idxContact);
                        if (dbContact == null) {
                            trace();
                        }
                        contactsByAbId.removeAt(idxContact);
                        if (abContact.phones.isEmpty()) {
                            storage.contacts.delete(dbContact);
                        } else {
                            List<ContactPhoneNumber> dbPhonesList = phonesByContacts.get(dbContact._id);
                            if (dbPhonesList == null) {
                                dbPhonesList = Collections.emptyList();
                            }
                            LongSparseArray<ContactPhoneNumber> dbPhones = query(dbPhonesList).toLongSparseArray(p -> p.link.abPhoneId);

                            for (ContactPhoneNumber abPhone : abContact.phones) {
                                int idxPhone = dbPhones.indexOfKey(abPhone.link.abPhoneId);
                                ContactPhoneNumber dbPhone;
                                if (idxPhone < 0) {
                                    dbPhone = phonesByNumber.get(abPhone.normalized);
                                    if (dbPhone == null) {
                                        dbPhone = abPhone.clone();
                                        storage.phoneNumbers.save(dbPhone);
                                        phonesByNumber.put(dbPhone.normalized, dbPhone);
                                        phonesByAbId.put(dbPhone.link.abPhoneId, dbPhone);
                                    } else {
                                        dbPhone.link = abPhone.link;
                                    }
                                    dbPhone.link.phone = dbPhone._id;
                                    dbPhone.link.contact = dbContact._id;
                                    storage.contactsPhonesLinks.save(dbPhone.link);
                                } else {
                                    dbPhones.removeAt(idxContact);
                                }
                            }
                            int i = dbPhones.size();
                            while (--i >= 0) {
                                storage.contactsPhonesLinks.delete(dbPhones.valueAt(i).link);
                            }

                        }

                    }
                }
                int i = contactsByAbId.size();
                while (--i >= 0) {
                    storage.contacts.delete(contactsByAbId.valueAt(i));
                }


                transaction.commit();
            }
        }
//        DebugUtils.importFile(app());
    }

}
