package app.laiki.service.ab;

import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import java.util.List;

import app.laiki.model.AppData;
import app.laiki.model.entities.Contact;
import app.laiki.utils.Utils;

import static app.laiki.App.data;
import static app.laiki.toolkit.collections.Query.query;

public class AddressBookSyncHelper {

    public static void doSync(Context context) {
        try (AbReader abReader = new AbReader(context)) {
            AppData storage = data();
            List<Contact> contacts = storage.contacts.selectAll().toList();
            LongSparseArray<Contact> contactsByAbId = query(contacts).toLongSparseArray(c -> c.abContactId);

            try (AppData.Transaction transaction = storage.beginTransaction()) {
                for (SyncUnit abContact : abReader) {
                    if (abContact.contact.abPhoneId <= 0 || TextUtils.isEmpty(abContact.contact.phone))
                        continue;

                    int idxContact = contactsByAbId.indexOfKey(abContact.contact.abContactId);
                    if (idxContact < 0) {
                        // новый контакт
                        abContact.contact.onUpdateName();
                        abContact.contact.serverId = Utils.md5(abContact.contact.phone);
                        storage.contacts.save(abContact.contact);
                    } else {
                        // существующий контакт
                        Contact dbContact = contactsByAbId.valueAt(idxContact);
                        contactsByAbId.removeAt(idxContact);

                        if (dbContact.addressBookDataChanged(abContact.contact)) {
                            abContact.contact._id = dbContact._id;
                            dbContact = abContact.contact;
                            dbContact.onUpdateName();
                            dbContact.serverId = Utils.md5(dbContact.phone);
                            storage.contacts.save(dbContact);
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
