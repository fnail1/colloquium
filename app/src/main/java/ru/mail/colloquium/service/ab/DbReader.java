package ru.mail.colloquium.service.ab;

import java.io.Closeable;
import java.util.Iterator;

import ru.mail.colloquium.model.entities.Contact;
import ru.mail.colloquium.toolkit.data.CursorWrapper;

public class DbReader implements Iterable<SyncUnit>, Closeable {
    private final CursorWrapper<Contact> cursor;

    public DbReader(CursorWrapper<Contact> cursor) {
        this.cursor = cursor;
    }

    @Override
    public Iterator<SyncUnit> iterator() {
        return new Iter(cursor);
    }

    @Override
    public void close() {
        cursor.close();
    }

    private class Iter implements Iterator<SyncUnit> {
        private final Iterator<Contact> cursor;
        private SyncUnit aggr;

        public Iter(CursorWrapper<Contact> cursor) {
            this.cursor = cursor.iterator();
            if (this.cursor.hasNext()) {
                aggr = new SyncUnit(this.cursor.next());
            }
        }

        @Override
        public boolean hasNext() {
            return aggr != null;
        }

        @Override
        public SyncUnit next() {
            SyncUnit unit = aggr;
            Contact next = null;

            while (cursor.hasNext() && (next = cursor.next()).abContactId == unit.contact.abContactId) {
                unit.merge(next);
                next = null;
            }
            aggr = next == null ? null : new SyncUnit(next);

            return unit;

        }
    }
}
