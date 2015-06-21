package nth.com.ares.domains;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 27/07/2014.
 */
public class MensajeDbHelper extends DbHelper {

    private static final String LOG = "MensajeDbHelper";

    public static final String KEY_BODY = "body";
    public static final String KEY_FROM = "de";
    public static final String KEY_TIPO = "tipo";
    public static final String KEY_VISTO = "visto";
    public static final String TABLE_MENSAJE = "mensaje";

    public static final String[] KEYS = {KEY_BODY,KEY_FROM,KEY_TIPO,KEY_VISTO};

    public MensajeDbHelper(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENSAJE);

        // create new tables
        onCreate(db);
    }

    public long createMensaje(Mensaje mensaje) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = setValues(mensaje, true);

        // insert row
        long res = db.insert(TABLE_MENSAJE, null, values);
        db.close();
        return res;
    }

    public Mensaje getMensaje(long mensaje_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_MENSAJE + " WHERE "
                + KEY_ID + " = " + mensaje_id;

        Cursor c = db.rawQuery(selectQuery, null);
        Mensaje cl = null;
        if (c.getCount() > 0) {
            c.moveToFirst();
            cl = setDatos(c);
        }
        db.close();
        return cl;
    }

    public ArrayList<Mensaje> getAllMensajees() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Mensaje> mensajees = new ArrayList<Mensaje>();
        String selectQuery = "SELECT  * FROM " + TABLE_MENSAJE;

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Mensaje cl = setDatos(c);

                // adding to tags list
                mensajees.add(cl);
            } while (c.moveToNext());
        }
        db.close();
        return mensajees;
    }

    public ArrayList<Mensaje> getOnlyMensajees() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Mensaje> mensajees = new ArrayList<Mensaje>();
        String selectQuery = "SELECT  * FROM " + TABLE_MENSAJE +
                " WHERE " + KEY_BODY + " <> 'none'";

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Mensaje cl = setDatos(c);

                // adding to tags list
                mensajees.add(cl);
            } while (c.moveToNext());
        }
        db.close();
        return mensajees;
    }

    public List<Mensaje> getAllMensajeesByVisto(String visto) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Mensaje> mensajees = new ArrayList<Mensaje>();
        String selectQuery = "SELECT  * FROM " + TABLE_MENSAJE +
                " WHERE " + KEY_VISTO + " = '" + visto + "'";
        if(visto=="S")
            selectQuery+=" order by fecha desc limit 10 ";
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Mensaje cl = setDatos(c);

                // adding to tags list
                if(visto=="S")
                    mensajees.add(0,cl);
                else
                    mensajees.add(cl);
            } while (c.moveToNext());
        }
        db.close();

        return mensajees;
    }

    public int countAllMensajees() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  count(*) count FROM " + TABLE_MENSAJE;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            int count = c.getInt(c.getColumnIndex("count"));
            db.close();
            return count;
        }
        db.close();
        return 0;
    }

    public int countMensajeesByNombre(String mensaje) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  count(*) count FROM " + TABLE_MENSAJE +
                " WHERE " + KEY_BODY + " = '" + mensaje + "'";
        Cursor c = db.rawQuery(selectQuery, null);
        db.close();
        if (c.moveToFirst()) {
            int count = c.getInt(c.getColumnIndex("count"));
            db.close();
            return count;
        }
        db.close();
        return 0;
    }
    public int countMensajeesByFecha(String fecha) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  count(*) count FROM " + TABLE_MENSAJE +
                " WHERE " + KEY_FECHA + " = '" + fecha + "'";
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            int count = c.getInt(c.getColumnIndex("count"));
            db.close();
            return count;
        }
        db.close();
        return 0;
    }

    public int updateMensaje(Mensaje mensaje) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = setValues(mensaje);

        // updating row
        int res = db.update(TABLE_MENSAJE, values, KEY_ID + " = ?",
                new String[]{String.valueOf(mensaje.getId())});
        db.close();
        return res;
    }

    public void deleteMensaje(Mensaje mensaje) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MENSAJE, KEY_ID + " = ?",
                new String[]{String.valueOf(mensaje.id)});
        db.close();
    }

    public void deleteAllMensajees() {
        String sql = "DELETE FROM " + TABLE_MENSAJE;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    private Mensaje setDatos(Cursor c) {
        Mensaje cl = new Mensaje(this.context);
        cl.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        cl.setFecha(c.getString(c.getColumnIndex(KEY_FECHA)));
        cl.setBody(c.getString(c.getColumnIndex(KEY_BODY)));
        cl.setSender(c.getString(c.getColumnIndex(KEY_FROM)));
        cl.setVisto(c.getString(c.getColumnIndex(KEY_VISTO)));
        cl.setMensajeDbHelper();
        return cl;
    }

    private ContentValues setValues(Mensaje mensaje, boolean fecha) {
        ContentValues values = new ContentValues();
        if (fecha) {
            values.put(KEY_FECHA, getDateTime());
        }
        values.put(KEY_BODY, mensaje.body);
        values.put(KEY_FROM, mensaje.sender);
        values.put(KEY_VISTO, mensaje.visto);
        return values;
    }

    private ContentValues setValues(Mensaje mensaje) {
        return setValues(mensaje, false);
    }
}

