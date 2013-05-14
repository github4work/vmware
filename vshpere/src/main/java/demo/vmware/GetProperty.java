package demo.vmware;

import com.vmware.vim25.*;

import java.util.*;

public class GetProperty extends Command {

    @Override
    public void onExecute() throws Exception {
        getProperty();
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
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    private Map<String, ManagedObjectReference> getMOREFsInFolderByType(ManagedObjectReference folder, String morefType)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        System.out.println("folder type : " + folder.getType());
        System.out.println("folder value: " + folder.getValue());

        Map<String, ManagedObjectReference> tgtMoref = new HashMap<String, ManagedObjectReference>();

        // Create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(folder);
        objectSpec.setSkip(Boolean.FALSE);

        // Create Property Spec
        PropertySpec namePropertySpec = new PropertySpec();
        namePropertySpec.setAll(Boolean.FALSE);
        namePropertySpec.setType(morefType);
        namePropertySpec.getPathSet().add("name");

        PropertySpec overallStatusPropertySpec = new PropertySpec();
        overallStatusPropertySpec.setAll(Boolean.FALSE);
        overallStatusPropertySpec.setType(morefType);
        overallStatusPropertySpec.getPathSet().add("overallStatus");

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getObjectSet().add(objectSpec);
        propertyFilterSpec.getPropSet().add(namePropertySpec);
        propertyFilterSpec.getPropSet().add(overallStatusPropertySpec);

        List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        List<ObjectContent> oCont = vimPort.retrieveProperties(serviceContent.getPropertyCollector(), propertyFilterSpecs);
        if (oCont != null) {
            System.out.println("Retrieve " + oCont.size() + " of ObjectContent.");
            for (ObjectContent oc : oCont) {
                ManagedObjectReference mr = oc.getObj();
                String entityNm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        entityNm = String.valueOf(dp.getVal());
                        System.out.println("[" + dp.getName() + "->" + dp.getVal() + "]");
                    }
                }
                tgtMoref.put(entityNm, mr);
            }
        }
        return tgtMoref;
    }

    private void getProperty() throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        Map<String, ManagedObjectReference> inventory =
                getMOREFsInFolderByType(rootRef, "ManagedEntity");
        for (String entityName : inventory.keySet()) {
            System.out.println("> " + inventory.get(entityName).getType() + ":"
                    + inventory.get(entityName).getValue() + "{" + entityName + "}");
        }
    }

    public static void main(String[] args) {
        Command command = new GetProperty();
        command.execute(args);
    }
}
