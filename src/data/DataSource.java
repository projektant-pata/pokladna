package data;

import java.util.List;

public interface DataSource extends AutoCloseable{
    List<Product> getAll();
    Product get(int index);
    boolean remove(int index);
    boolean save(Product product);
}
