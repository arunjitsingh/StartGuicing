package guicing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import guicing.proto.StorageProto.Item;
import guicing.proto.StorageProto.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class FileStorageService implements StorageService<Item, Long> {

  private final String fileName;
  private final Map<Long, Item> cache;

  private boolean opened;
  private long nextId;

  private FileStorageService(String fileName) {
    this.fileName = fileName;
    cache = Maps.newHashMap();
    opened = false;
  }

  @Override
  public void start() {
    if (opened) {
      throw new IllegalStateException("Store is already opened");
    }
    try {
      Store store;
      File file = new File(fileName);
      if (file.exists()) {
        InputStream is = new FileInputStream(file);
        store = Store.parseFrom(is);
        is.close();
      } else {
        store = Store.newBuilder().setNextId(0).build();
      }
      opened = true;
      createNewInMemoryCache(store);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public void stop() {
    checkOpened();
    Store store = Store.newBuilder()
        .setNextId(nextId)
        .addAllItem(cache.values()).build();
    File file = new File(fileName);
    try {
      OutputStream os = new FileOutputStream(file);
      store.writeTo(os);
      os.close();
      opened = false;
      nextId = 0;
      cache.clear();
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException(fnfe);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public Item get(Long id) {
    checkOpened();
    return cache.get(id);
  }

  @Override
  public List<Item> list() {
    checkOpened();
    return ImmutableList.<Item>builder().addAll(cache.values()).build();
  }

  @Override
  public Long save(Item t) {
    checkOpened();
    long id = nextId;
    cache.put(id, t);
    nextId++;
    return id;
  }

  @Override
  public void delete(Long id) {
    checkOpened();
    cache.remove(id);
  }

  @Override
  public Long getNextId() {
    return nextId;
  }

  private void createNewInMemoryCache(final Store store) {
    checkOpened();
    nextId = store.getNextId();
    int count = store.getItemCount();
    for (int i = 0; i < count; ++i) {
      Item item = store.getItem(i);
      cache.put(item.getId(), item);
    }
  }

  private void checkOpened() {
    if (!opened) {
      throw new IllegalStateException("Store is closed");
    }
  }

  private static FileStorageService instance = null;

  public static FileStorageService openStore(String fileName) {
    if (instance == null) {
      instance = new FileStorageService(fileName);
    } else if (!instance.opened) {
    } else {
      throw new RuntimeException("Store is already opened with file: " + instance.fileName);
    }
    return instance;
  }

  public static FileStorageService getInstance() {
    return instance;
  }

}
