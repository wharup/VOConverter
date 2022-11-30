package todto.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class SampleHandler extends AbstractHandler {

	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Command command = event.getCommand();
     
        switch (command.getId()) {
            case "toDTO.commands.sampleCommand":
                return executeGenerateConvertMethods(event);
            case "toDTO.commands.sampleCommand2":
                return executeGenerateAndConvertToCopyUtil(event);
            default:
                return null;
        }
    }

    private Object executeGenerateAndConvertToCopyUtil(ExecutionEvent event) throws ExecutionException {
        String result = "";
        ConvertController controller = new ConvertController();
        try {
            result = controller.generateAndConvertToCopyUtil(event);
        } catch (ConvertException e) {
            showErrorMessage(event, e.getMessage());
            return null;
        }
        showSuccessMessage(event, result);
        
        return null;
    }

    private Object executeGenerateConvertMethods(ExecutionEvent event) throws ExecutionException {
        String result = "";
	    ConvertController controller = new ConvertController();
	    try {
	        result = controller.generateConvertMethods(event);
        } catch (ConvertException e) {
            showErrorMessage(event, e.getMessage());
            return null;
        }
	    showSuccessMessage(event, result);
	    
		return null;
    }

    private void showErrorMessage(ExecutionEvent event, String msg) throws ExecutionException {
        showMessage(event, "Error", msg);
    }

    private void showSuccessMessage(ExecutionEvent event, String msg) throws ExecutionException {
        showMessage(event, "Success", msg);
    }
    
    private void showMessage(ExecutionEvent event, String title, String msg) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        MessageDialog.openInformation(
				window.getShell(),
				title,
				msg);
    }
    
}
