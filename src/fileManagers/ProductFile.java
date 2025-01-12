package fileManagers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.Product;
import data.SourceException;

public class ProductFile {
    private RandomAccessFile file;
    private final Charset CHARSET = StandardCharsets.UTF_8;

    public ProductFile() throws SourceException{
        try {
            file = new RandomAccessFile("/home/patrik/javaprograms/pokladna/pokladna/src/files/products.dat", "rw");
        } catch (FileNotFoundException e) {
         throw new SourceException(e.getMessage(), e);
        }
    }

    public List<Product> getAll(){
        List<Product> list = new ArrayList<>();

        try{
            file.seek(0);
            int index = 0;
            while (index * Product.DATA_SIZE < file.length()) {
                list.add(read());
                index++;
            }
            return list;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public Product get(int index){
        try{
            if (index * Product.DATA_SIZE >= file.length()) 
                return null;

            file.seek(index * Product.DATA_SIZE);
            return read();
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean save(Product product){
        try{
            file.seek(file.length());
            file.setLength(file.length() + Product.DATA_SIZE);
            return write(product);
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    } 

    public boolean remove(int index){
        try{
            if (index * Product.DATA_SIZE >= file.length()) 
                return false;

            while ((index + 1) * Product.DATA_SIZE < file.length()) {
                file.seek((index + 1) * Product.DATA_SIZE);
                byte[] buffer = new byte[Product.DATA_SIZE];
                file.read(buffer);
                file.seek(index * Product.DATA_SIZE);
                file.write(buffer);
            }
            file.setLength(file.length() - Product.DATA_SIZE);
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() throws Exception {
        file.close();
    }

    //Pomocne metody
    private Product read() throws IOException{
        byte[] buffer = new byte[Product.NAME_SIZE];
        file.read(buffer);
        String name = (new String(buffer, CHARSET).trim());

        Product product = new Product();
        product.setName(name);
        product.setPrice(file.readShort());
        
        return product;
    }
    private boolean write(Product product) throws IOException{
        file.write(Arrays.copyOf(product.getName().getBytes(CHARSET), Product.NAME_SIZE));
        file.writeShort(product.getPrice());
        
        return true;
    }
}
