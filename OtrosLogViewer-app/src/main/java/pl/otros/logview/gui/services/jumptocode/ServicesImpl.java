package pl.otros.logview.gui.services.jumptocode;

import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.services.Services;
import pl.otros.logview.gui.services.persist.PersistService;
import pl.otros.logview.gui.services.persist.SerializePersisService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerService;
import pl.otros.logview.gui.services.tasks.TaskSchedulerServiceImpl;

public class ServicesImpl implements Services {
  private JumpToCodeService jumpToCodeService;
  private TaskSchedulerServiceImpl taskSchedulerService;
  private PersistService persistService;

  public ServicesImpl(OtrosApplication otrosApplication) {
    jumpToCodeService = new JumpToCodeServiceImpl(otrosApplication.getConfiguration());
    taskSchedulerService = new TaskSchedulerServiceImpl();
    persistService = new SerializePersisService();
  }

  @Override
  public JumpToCodeService getJumpToCodeService() {
    return jumpToCodeService;
  }

  @Override
  public TaskSchedulerService getTaskSchedulerService() {
    return taskSchedulerService;
  }

  @Override
  public PersistService getPersistService() {
    return persistService;
  }
}
