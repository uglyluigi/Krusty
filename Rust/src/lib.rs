mod krustynative;

#[cfg(test)]
mod tests {
    #[test]
    fn test_blur() {
        crate::krustynative::blur_image_impl("./picture_input/trump.jpg", 100);
        use std::fs;
        assert_eq!(fs::metadata("./output.png").is_ok(), true)
    }
}