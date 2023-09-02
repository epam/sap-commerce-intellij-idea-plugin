class BooClass {

    public void foo() {
        final CustomerModel customer = new CustomerModel();
        // non-navigable inlay hint
        customer./*<# dynamic #>*/getAllSearchRestrictions();
        // navigable inlay hint
        customer./*<# dynamic⌝ #>*/getAllGroups();
        customer.getName();
    }
}
