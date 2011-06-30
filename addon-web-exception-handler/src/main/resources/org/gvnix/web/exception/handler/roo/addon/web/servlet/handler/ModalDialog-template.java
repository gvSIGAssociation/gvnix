package ${PACKAGE};

import java.util.HashMap;


/**
 * Bean holding data about the ModalDialog we want to render with the help
 * of <code>message-box.tagx</code> TAGx.
 *
 * <ul>
 *   <li>dialogType: informs about the type of the Dialog: Error, Alert, Info, Suggest.</li>
 *   <li>page: is the absolute path in app. context of the JSPx we want to include in
 *   the dialog. i.e: <code>/WEB-INF/views/myIncludedPage.jspx</code></li>
 *   <li>exception: Exception object to be shown in the dialog. The best idea is to show it
 *   in the included JSPx.</li>
 *   <li>title: short message to be shown in the dialog box. Must be a key of the i18n
 *   messages bundle</li>
 *   <li>description: large message to be shown in the dialog box. Must be a key of the i18n
 *   messages bundle</li>
 *   <li>params: HashMap with String as key and Object as value. Useful if other data is needed
 *   in the included JSPx</li>
 * </ul>
 *
 */
public class ModalDialog {

  public enum DialogType {
    Error, Info, Alert, Suggest;
  }

  private DialogType dialogType;

  private String page;

  private Exception exception;

  private String title;

  private String description;

  private HashMap<String, Object> params;

  public ModalDialog(DialogType dialogType, String page,
      Exception exception, String title, String description,
      HashMap<String, Object> params) {
    this.dialogType = dialogType;
    this.page = page;
    this.exception = exception;
    this.title = title;
    this.description = description;
    this.params = params;
  }

  public ModalDialog(String page, Exception exception) {
    this.dialogType = DialogType.Error;
    this.page = page;
    this.exception = exception;
    this.title = "message_error_title";
  }

  public ModalDialog(DialogType dialogType, String title, String description) {
    this.dialogType = dialogType;
    this.title = title;
    this.description = description;
  }

  public ModalDialog(DialogType dialogType, String page, String title, String description,
      HashMap<String, Object> params) {
    this.dialogType = dialogType;
    this.page = page;
    this.title = title;
    this.description = description;
    this.params = params;
  }

  public DialogType getDialogType() {
    return dialogType;
  }

  public void setDialogType(DialogType dialogType) {
    this.dialogType = dialogType;
  }

  public String getPage() {
    return page;
  }

  public void setPage(String page) {
    this.page = page;
  }

  public Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public HashMap<String, Object> getParams() {
    return params;
  }

  public void setParams(HashMap<String, Object> params) {
    this.params = params;
  }

  @Override
  public String toString() {
    return "ModalDialog [dialogType=".concat(dialogType.name())
        .concat(", page=").concat(page)
        .concat(", exception=").concat(exception.toString())
        .concat(", title=").concat(title)
        .concat(", description=").concat(description)
        .concat(", params=").concat(params.toString()).concat("]");
  }

}
