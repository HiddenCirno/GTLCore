# GTL 蒸汽多方块（LV 前可制造）参考报告

> 目的：为「梦想电梯 → 蒸汽工业加工厂」模块确定配方覆盖、并行和速度基线。
>
> 数据来源：GTLCore `MultiBlockMachineA` 蒸汽多方块定义 + `LargeSteamParallelMultiblockMachine` / `GTLRecipeModifiers` + GTL-1454 KubeJS 控制器配方。

---

## 1. 结论摘要

GTL 目前新增的蒸汽加工多方块可以分成两组：

| 组 | 并行 | 基础速度 | 超频 | 定位 |
|---|---:|---|---|---|
| **小型蒸汽多方块** | 8 | 1× | 无原生加速；大型蒸汽仓可 OC | 蒸汽时代的基础多方块 |
| **大型蒸汽多方块** | 64 | 2×（duration ×0.5） | 大型蒸汽仓 OC 0–4 | 蒸汽时代的进阶多方块 |

所有机器的配方输入上限：

- 普通蒸汽仓：**EUt ≤ 32**
- 大型蒸汽仓：**EUt ≤ 128**

普通蒸汽仓转换率：

```text
0.5 EU / mB 蒸汽
```

大型蒸汽仓：

```text
0.5 × 3^OC EU / mB 蒸汽
duration × 0.5 / 2^OC
```

> 这意味着：蒸汽多方块不是“无限并行”；并行受输入/输出、蒸汽量和 EUt 上限共同限制。

---

## 2. 机器清单

### 2.1 小型蒸汽多方块（8 并行）

| 机器 ID | 配方类型 | 并行 | 速度 | 备注 |
|---|---|---:|---|---|
| `steam_piston_hammer` | Forge Hammer | 8 | 1× | 蒸汽锻锤 |
| `steam_pressor` | Compressor | 8 | 1× | 蒸汽压缩机 |
| `steam_foundry` | Alloy Smelter | 8 | 1× | 蒸汽合金炉 |
| `steam_mixer` | Mixer | 8 | 1× | 蒸汽混合机 |
| `steam_bath` | Chemical Bath | 8 | 1× | 蒸汽化学浴 |
| `steam_ore_washer` | Ore Washer | 8 | 1× | 蒸汽洗矿机 |

### 2.2 大型蒸汽多方块（64 并行）

| 机器 ID | 配方类型 | 并行 | 速度 | 备注 |
|---|---|---:|---|---|
| `large_steam_macerator` | Macerator | 64 | 2× | 大型蒸汽研磨机 |
| `large_steam_circuit_assembler` | Circuit Assembler | 64 | 2× | 控制器配方需要 AE2 分子装配器 |
| `large_steam_mixer` | Mixer | 64 | 2× | 大型蒸汽混合机 |
| `large_steam_centrifuge` | Centrifuge | 64 | 2× | 大型蒸汽离心机 |
| `large_steam_thermal_centrifuge` | Thermal Centrifuge | 64 | 2× | 大型蒸汽热离心机 |
| `large_steam_bath` | Chemical Bath | 64 | 2× | 大型蒸汽化学浴 |
| `large_steam_ore_washer` | Ore Washer | 64 | 2× | 大型蒸汽洗矿机 |

### 2.3 排除项

以下机器虽然名字带 steam，但不属于 LV 蒸汽加工中心：

- `dimensionally_transcendent_steam_oven`：524,288 并行，后期机器；
- `dimensionally_transcendent_steam_boiler`：锅炉，不是加工配方；
- `supercritical_steam_turbine` / `steam_mega_turbine` / `supercritical_mega_steam_turbine`：发电，不是加工配方。

---

## 3. 配方类型覆盖范围

GTL 蒸汽多方块目前覆盖的加工类型：

```text
FORGE_HAMMER
COMPRESSOR
ALLOY_SMELTER
MACERATOR
CIRCUIT_ASSEMBLER
MIXER
CENTRIFUGE
THERMAL_CENTRIFUGE
CHEMICAL_BATH
ORE_WASHER
```

如果「蒸汽工业加工厂」要“处理所有蒸汽加工配方”，建议以这 10 类作为第一版范围。

### 尚未覆盖、但可能属于“蒸汽加工”的类型

GTCEu 还有一些蒸汽单方块机器/配方类型，GTL 没有对应的蒸汽多方块，例如：

- 蒸汽提取机 / Extractor
- 蒸汽线材轧机 / Wiremill
- 蒸汽压弯机 / Bender
- 蒸汽挤出机 / Extruder
- 蒸汽车床 / Lathe
- 蒸汽切割机 / Cutting

这些是否纳入「蒸汽工业加工厂」需要单独决定。如果纳入，模块的配方覆盖就不只是 GTL 蒸汽多方块，而是“所有 GTCEu 蒸汽机器配方类型”。

---

## 4. 等级与可制造性

按控制器配方材料判断：

- 小型蒸汽多方块：青铜/钢/蒸汽组件，**蒸汽阶段即可制造**；
- 大型蒸汽多方块：青铜/钢/蒸汽组件 + `precision_steam_mechanism`，**蒸汽阶段可制造**；
- `large_steam_circuit_assembler` 额外需要 `ae2:molecular_assembler`，受 AE2 进度影响，但不是 LV 电压门槛；
- `dimensionally_transcendent_steam_oven` 属于后期，不纳入 LV 参考。

所以：

> 表 2.1 / 2.2 的 13 台机器，按控制器配方判断都属于蒸汽/LV 前可制造范围。

---

## 5. 对「蒸汽工业加工厂」的数值参考

### 5.1 基础并行

GTL 蒸汽多方块的基线：

- 小型：8 并行；
- 大型：64 并行。

建议模块**自身基础并行**不要直接按梦想电梯的满级 256 设计，否则会和轨道加成叠加过高：

| 模块 Tier | 参考基础并行 |
|---|---:|
| T1 | 8（小型蒸汽多方块水平） |
| T2 | 16–32 |
| T3 | 64（大型蒸汽多方块水平） |

轨道提供的 32–256 并行作为**整机共享池**再叠加/限制。

### 5.2 基础速度

- 小型蒸汽多方块：1×；
- 大型蒸汽多方块：2×；
- 大型蒸汽仓 OC：最多 4 级，理论 duration 再 /16。

梦想电梯轨道已经提供 1.5×–4× 速度。建议：

- 模块自身速度保持在 **1×–2×**；
- 不要再把“大型蒸汽仓 2× + OC 16× + 轨道 4×”全部叠满；
- 否则单模块速度会远超蒸汽时代甚至 HV 机器的合理范围。

### 5.3 蒸汽消耗锚点

普通蒸汽仓：

```text
1 EU/t 需要 2 mB/t 蒸汽
```

例：

- 32 EU/t 配方 → 64 mB/t 蒸汽；
- 8 并行 × 32 EU/t → 512 mB/t 蒸汽。

大型蒸汽仓 OC0：

```text
1 EU/t 需要 2 mB/t 蒸汽
```

OC 后转换率变化，需要按 `0.5 × 3^OC` 计算。

如果梦想电梯满级有 256 整机并行 + 4× 速度，蒸汽消耗会非常夸张——这正是“巨型蒸汽输入仓”存在的意义。

### 5.4 EUt 上限

建议明确模块的配方电压上限：

- 普通蒸汽模式：EUt ≤ 32；
- 大型蒸汽模式：EUt ≤ 128；
- 如果希望模块处理 HV 配方，需要额外的“高压蒸汽”设定，不要直接放开。

---

## 6. 第一版建议

1. 「蒸汽工业加工厂」第一版覆盖 10 类 GTL 蒸汽多方块配方：
   - FORGE_HAMMER
   - COMPRESSOR
   - ALLOY_SMELTER
   - MACERATOR
   - CIRCUIT_ASSEMBLER
   - MIXER
   - CENTRIFUGE
   - THERMAL_CENTRIFUGE
   - CHEMICAL_BATH
   - ORE_WASHER
2. 模块基础并行按 **8 / 16–32 / 64** 三档设计；
3. 模块基础速度按 **1×–2×** 设计；
4. 轨道并行作为整机共享池，不按模块复制；
5. 蒸汽消耗按并行和速度增长；
6. 单方块蒸汽配方（extractor/wiremill/bender 等）先不纳入，避免第一版范围失控。

---

## 7. 待确认

- 是否把 GTCEu 蒸汽单方块配方类型也纳入“所有蒸汽加工配方”；
- 模块自身是否有 Tier；
- 模块 Tier 与轨道等级是否叠加；
- 大型蒸汽仓 OC 是否在梦想电梯中保留；
- 模块自身的 EUt 上限。
