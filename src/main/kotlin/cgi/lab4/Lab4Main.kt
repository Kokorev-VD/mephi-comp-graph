package cgi.lab4

import common.Image8bpp
import common.addGaussianNoise
import common.addSaltAndPepperNoise

fun main() {
    println("Лабораторная работа No 4: Пространственная фильтрация изображений\n")

    val srcPath = "src/main/resources/common/cat.png"
    val orig = Image8bpp.load(srcPath)
    orig.save("00_original.png")
    println("Исходное изображение: $srcPath, ${orig.width} x ${orig.height}\n")

    println("Задача 1. ФНЧ — гауссово ядро произвольного размера на AWGN (σ=20)")
    val awgn = addGaussianNoise(orig, sigma = 20.0)
    awgn.save("01_awgn_input.png")

    for (sz in listOf(3, 5, 7)) {
        val lpf = lowPassGaussian(awgn, kernelSize = sz)
        lpf.save("01_awgn_lpf_gauss_${sz}x${sz}.png")
        println("  Гауссов ФНЧ ${sz}x${sz} → 01_awgn_lpf_gauss_${sz}x${sz}.png")
    }
    println()

    println("Задача 2. ФНЧ на изображении с импульсным шумом (p=0.05)")
    val sap = addSaltAndPepperNoise(orig, probability = 0.05)
    sap.save("02_saltpepper_input.png")
    val sapLpf = lowPassGaussian(sap, kernelSize = 5)
    sapLpf.save("02_saltpepper_lpf_gauss.png")
    println("  Гауссов ФНЧ 5x5 → 02_saltpepper_lpf_gauss.png")

    println("Задача 3. Усредняющий фильтр с порогом (T=20) на AWGN")
    val avgT = averagingWithThreshold(awgn, threshold = 20)
    avgT.save("03_averaging_with_threshold.png")
    println("  → 03_averaging_with_threshold.png\n")

    println("Задача 4. ФВЧ — лапласиан и LoG произвольного размера")
    for (sz in listOf(3, 5)) {
        val lap = highPassLaplacian(orig, kernelSize = sz)
        lap.save("04_hpf_laplacian_${sz}x${sz}.png")
        println("  Лапласиан ${sz}x${sz} → 04_hpf_laplacian_${sz}x${sz}.png")
    }
    val logImg = highPassLoG(orig, sigma = 1.4)
    logImg.save("04_hpf_log.png")
    println("  LoG (σ=1.4) → 04_hpf_log.png\n")

    println("Задача 5. Повышение резкости (sharpening kernel: I + α·L)")
    for (alpha in listOf(0.5, 1.0)) {
        val sharp = sharpenConvolution(orig, kernelSize = 3, alpha = alpha)
        sharp.save("05_sharpen_alpha${alpha}.png")
        println("  Sharpening 3x3, α=$alpha → 05_sharpen_alpha${alpha}.png")
    }
    println()

    println("Задача 6. Детектирование границ через zero-crossing на LoG (σ=1.4)")
    val edges = edgesByZeroCrossing(orig, sigma = 1.4)
    edges.save("06_edges_zero_crossing.png")
    println("  → 06_edges_zero_crossing.png\n")

    println("Задача 7. Билатеральный фильтр (σ_s=2.0, σ_r=30.0, ядро 5x5)")
    val bilateralAwgn = bilateralFilter(awgn, kernelSize = 5, sigmaSpatial = 2.0, sigmaRange = 30.0)
    bilateralAwgn.save("07_bilateral_awgn.png")
    println("  На AWGN → 07_bilateral_awgn.png")

    val bilateralSap = bilateralFilter(sap, kernelSize = 5, sigmaSpatial = 2.0, sigmaRange = 30.0)
    bilateralSap.save("07_bilateral_saltpepper.png")
    println("  На импульсном шуме → 07_bilateral_saltpepper.png")
    println("  Прим.: сохраняет границы, но слаб против импульсного шума.\n")

    println("Задача 8. Сравнение фильтров при разных размерах ядер")
    for (sz in listOf(3, 7, 11)) {
        val gauss = lowPassGaussian(awgn, kernelSize = sz)
        gauss.save("08_compare_gauss_${sz}x${sz}.png")
        val bilat = bilateralFilter(awgn, kernelSize = sz, sigmaSpatial = (sz - 1) / 6.0, sigmaRange = 30.0)
        bilat.save("08_compare_bilateral_${sz}x${sz}.png")
        println("  Ядро ${sz}x${sz}: гаусс → 08_compare_gauss_${sz}x${sz}.png, " +
                "билатеральный → 08_compare_bilateral_${sz}x${sz}.png")
    }
    println()

    println("AWGN → гауссов ФНЧ или билатеральный; импульсный шум → медианный (ЛР5);")
    println("границы → LoG + zero-crossing; резкость → sharpening kernel (I + α·L).\n")

    println("Результаты сохранены: src/main/resources/cgi/lab4/")
}
