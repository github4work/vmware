package demo.vmware.host;

import com.vmware.vim25.*;
import demo.vmware.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * User: Ma Tao
 * Date: 5/13/13
 * Time: 3:52 PM
 */
public class ListHosts extends Command {

    @Override
    public void onExecute() throws Exception {
        listHosts();
    }

    private void listHosts() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference viewManager = serviceContent.getViewManager();
        ManagedObjectReference containerView = vimPort.createContainerView(viewManager, rootRef, Arrays.asList("HostSystem"), true);

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType("HostSystem");
        propertySpec.getPathSet().add("name");

        // Traversal Spec
        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("view");
        traversalSpec.setPath("view");
        traversalSpec.setSkip(Boolean.FALSE);
        traversalSpec.setType("ContainerView");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(containerView);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(traversalSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        List<ObjectContent> objContents =
                vimPort.retrieveProperties(serviceContent.getPropertyCollector(),
                        propertyFilterSpecs);

        if (null != objContents) {
            System.out.println("Retrieve " + objContents.size() + " of ObjectContent.");
            for (ObjectContent objContent : objContents) {
                ManagedObjectReference mr = objContent.getObj();
                String entityNm = null;
                List<DynamicProperty> dps = objContent.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        entityNm = (String) dp.getVal();
                        System.out.println(entityNm);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Command command = new ListHosts();
        command.execute(args);
    }
}
