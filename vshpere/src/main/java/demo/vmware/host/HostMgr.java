package demo.vmware.host;

import com.vmware.vim25.*;
import demo.vmware.Command;

import java.util.*;

/**
 * A simplest example to get a managed object's property.
 * User: github4work
 * Date: 5/13/13
 * Time: 11:16 AM
 */
public class HostMgr extends Command {
    private static final String PROP_ME_NAME = "name";

    @Override
    public void onExecute() throws Exception {
        printInventory();
    }

    /**
     * Returns all the MOREFs of the specified type that are present under the
     * folder
     *
     * @param folder    {@link ManagedObjectReference} of the folder to begin the search
     *                  from
     * @param morefType Type of the managed entity that needs to be searched
     * @return Map of name and MOREF of the managed objects present. If none
     *         exist then empty Map is returned
     * @throws com.vmware.vim25.InvalidPropertyFaultMsg
     *
     * @throws com.vmware.vim25.RuntimeFaultFaultMsg
     *
     */
    private Map<String, ManagedObjectReference> getMOREFsInFolderByType(
            ManagedObjectReference folder, String morefType)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference viewManager = serviceContent.getViewManager();
        ManagedObjectReference containerView =
                vimPort.createContainerView(viewManager, folder,
                        Arrays.asList(morefType), true);

        Map<String, ManagedObjectReference> tgtMoref =
                new HashMap<String, ManagedObjectReference>();

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType(morefType);
        propertySpec.getPathSet().add(PROP_ME_NAME);

        TraversalSpec ts = new TraversalSpec();
        ts.setName("view");
        ts.setPath("view");
        ts.setSkip(false);
        ts.setType("ContainerView");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(containerView);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(ts);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> propertyFilterSpecs =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        List<ObjectContent> oCont =
                vimPort.retrieveProperties(serviceContent.getPropertyCollector(),
                        propertyFilterSpecs);
        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                ManagedObjectReference mr = oc.getObj();
                String entityNm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        entityNm = (String) dp.getVal();
                    }
                }
                tgtMoref.put(entityNm, mr);
            }
        }
        return tgtMoref;
    }

    private void printInventory() throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> inventory =
                getMOREFsInFolderByType(rootRef, "ManagedEntity");
        for (String entityName : inventory.keySet()) {
            ManagedObjectReference mor = inventory.get(entityName);
            System.out.println(mor);
            System.out.println("> " + inventory.get(entityName).getType() + ":"
                    + inventory.get(entityName).getValue() + "{" + entityName + "}");
        }
    }

    public static void main(String[] args) {
        Command command = new HostMgr();
        command.execute(args);
    }
}
