use j4rs::prelude::*;
use j4rs_derive::*;

#[call_from_java("bindings.RustDefs.exampleMethod")]
fn example_method() {
    println!("Justin is lame");
}