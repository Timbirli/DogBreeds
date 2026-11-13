package com.example.dogbreeds.domain

/** Выбирает адрес картинки: прямой URL, иначе по reference_image_id, иначе пустая строка. */
class ResolveImageUrlUseCase {
    operator fun invoke(imageUrl: String?, referenceImageId: String?): String = when {
        !imageUrl.isNullOrBlank() -> imageUrl
        !referenceImageId.isNullOrBlank() -> "https://cdn2.thedogapi.com/images/$referenceImageId.jpg"
        else -> ""
    }
}
