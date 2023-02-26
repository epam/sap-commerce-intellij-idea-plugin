class BooClass {

    public void foo() {
        final CustomerModel customer = new CustomerModel();
        customer.<# dynamic #>getAllGroups();
        customer.getName();
    }
}
