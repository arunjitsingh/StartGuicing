package guicing;

import static org.junit.Assert.*;

import com.google.protobuf.ByteString;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import guicing.proto.StorageProto.Item;

import java.io.File;

@RunWith(JUnit4.class)
public class FileStorageServiceTest {

  private static final String FILE_NAME = "/tmp/test.pbstore";

  FileStorageService service;

  private void restart() {
    service.stop();
    service.start();
  }

  @Before
  public void setUp() {
    service = FileStorageService.openStore(FILE_NAME);
    service.start();
  }

  @After
  public void tearDown() {
    service.stop();
    new File(FILE_NAME).delete();
  }

  @Test
  public void testEmptySize() {
    assertEquals(0, service.list().size());
  }

  @Test
  public void testSave() {
    Item item = Item.newBuilder()
        .setId(service.getNextId())
        .setSerializedItem(ByteString.EMPTY)
        .build();
    long id = service.save(item);
    assertEquals(id, item.getId());
    assertEquals(1, service.list().size());
    restart();
    assertEquals(1, service.list().size());
    assertEquals(ByteString.EMPTY, service.get(id).getSerializedItem());
  }
}
