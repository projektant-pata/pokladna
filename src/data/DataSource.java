package data;

import java.util.List;

public interface DataSource extends AutoCloseable{
    List<Bill> getAll();
    Bill getOne(int index);
    boolean delete(int index);
    boolean save(Bill bill);
    boolean save(Bill bill, int index);
}
