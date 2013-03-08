package guicing;

import java.util.List;

public interface StorageService<T, ID> extends Service {

  public T get(ID id);

  public List<T> list();

  public ID save(T t);

  public void delete(ID id);

  public ID getNextId();

}
