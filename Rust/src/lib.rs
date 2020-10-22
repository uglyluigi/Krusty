mod krustynative;

#[cfg(test)]
mod tests {
    #[test]
    fn test_blur() {
        crate::krustynative::blur_image_impl("./picture_input/pao.jpg", 100);
        use std::fs;
        assert_eq!(fs::metadata("./output.png").is_ok(), true)
    }

    #[test]
    fn test_blend() {
        crate::krustynative::blend_image_impl("./picture_input/berry.jpg", "./picture_input/watermark.png");
        use std::fs;
        assert_eq!(fs::metadata("./blend_output.png").is_ok(), true)
    }
}