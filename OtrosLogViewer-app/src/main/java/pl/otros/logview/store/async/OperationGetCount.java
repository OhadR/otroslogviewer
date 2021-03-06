package pl.otros.logview.store.async;

import pl.otros.logview.store.LogDataStore;

import java.util.concurrent.Callable;

class OperationGetCount implements Callable<Integer> {
  private final LogDataStore logDataStore;

  public OperationGetCount(LogDataStore logDataStore) {
    this.logDataStore = logDataStore;
  }

  @Override
  public Integer call() {
     return logDataStore.getCount();
  }
}
