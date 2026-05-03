package cgi.lab4

import common.Image8bpp

fun main() {
    println("Лабораторная работа No 4: Пространственная фильтрация изображений\n")

    val srcPath = "src/main/resources/common/cat.png"
    val orig = Image8bpp.load(srcPath)
    orig.save("00_original.png")
    println("Исходное изображение: $srcPath, ${orig.width} x ${orig.height}\n")

    println("Задача 1. ФНЧ (гауссово ядро 5x5) на изображении с AWGN (σ=20)")
    val task1Result = mutableListOf<String>()
    val awgn = addGaussianNoise(orig, sigma = 20.0)
    awgn.save("01_awgn_input.png")
    task1Result.add("01_awgn_input.png")
    val awgnLpf = lowPassGaussian(awgn)
    awgnLpf.save("01_awgn_lpf_gauss.png")
    task1Result.add("01_awgn_lpf_gauss.png")
    println("Результат задачи 1 сохранен: $task1Result\n")

    println("Задача 2. ФНЧ на изображении с импульсным шумом (p=0.05)")
    val task2Result = mutableListOf<String>()
    val sap = addSaltAndPepperNoise(orig, probability = 0.05)
    sap.save("02_saltpepper_input.png")
    task2Result.add("02_saltpepper_input.png")
    val sapLpf = lowPassGaussian(sap)
    sapLpf.save("02_saltpepper_lpf_gauss.png")
    task2Result.add("02_saltpepper_lpf_gauss.png")
    println("Замечание: гауссов ФНЧ плохо подавляет импульсный шум — шум смазывается")
    println("           пятнами, а не удаляется (требуется медианный фильтр).")
    println("Результат задачи 2 сохранен: $task2Result\n")

    println("Задача 3. Усредняющий фильтр с порогом (T=20) на изображении с AWGN")
    val task3Result = mutableListOf<String>()
    val avgT = averagingWithThreshold(awgn, threshold = 20)
    avgT.save("03_averaging_with_threshold.png")
    task3Result.add("03_averaging_with_threshold.png")
    println("Результат задачи 3 сохранен: $task3Result\n")

    println("Задача 4. ФВЧ — лапласиан 3x3 и LoG (σ=1.4)")
    val task4Result = mutableListOf<String>()
    val lap = highPassLaplacian(orig)
    lap.save("04_hpf_laplacian.png")
    task4Result.add("04_hpf_laplacian.png")
    val log = highPassLoG(orig, sigma = 1.4)
    log.save("04_hpf_log.png")
    task4Result.add("04_hpf_log.png")
    println("Результат задачи 4 сохранен: $task4Result\n")

    println("Задача 5. Повышение резкости через лапласиан (α=1.0)")
    val task5Result = mutableListOf<String>()
    val sharp = sharpen(orig, alpha = 1.0)
    sharp.save("05_sharpened.png")
    task5Result.add("05_sharpened.png")
    println("Результат задачи 5 сохранен: $task5Result\n")

    println("Задача 6. Детектирование границ через zero-crossing на LoG (σ=1.4)")
    val task6Result = mutableListOf<String>()
    val edges = edgesByZeroCrossing(orig, sigma = 1.4)
    edges.save("06_edges_zero_crossing.png")
    task6Result.add("06_edges_zero_crossing.png")
    println("Результат задачи 6 сохранен: $task6Result\n")

    println("\nРезультаты задач сохранены тут: src/main/resources/cgi/lab4/")
}
