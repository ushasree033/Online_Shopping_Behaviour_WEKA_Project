
import pandas as pd
import matplotlib.pyplot as plt
import os

# Folder containing the five CSV result files
folder = os.path.dirname(os.path.abspath(__file__))

def save_chart(filename):
    plt.tight_layout()
    plt.savefig(os.path.join(folder, filename), dpi=300, bbox_inches="tight")
    plt.close()
    print("Created:", filename)


# 1. ROLL-UP: Purchases by Product Category
df = pd.read_csv(os.path.join(folder, "Rollup_Result.csv"))

plt.figure(figsize=(9, 5))
plt.bar(df["Dimension"], df["Purchases"])
plt.title("Roll-up: Purchases by Product Category")
plt.xlabel("Product Category")
plt.ylabel("Number of Purchases")
plt.xticks(rotation=30)
save_chart("Rollup_Visualization.png")


# 2. DRILL-DOWN: Category, Visitor and Device
df = pd.read_csv(os.path.join(folder, "Drilldown_Result.csv"))

parts = df["Dimension"].str.split(" | ", regex=False, expand=True)
df["Category"] = parts[0]
df["Visitor"] = parts[1]
df["Device"] = parts[2]
df["Group"] = df["Visitor"] + " - " + df["Device"]

pivot = df.pivot_table(
    index="Category",
    columns="Group",
    values="Purchases",
    aggfunc="sum",
    fill_value=0
)

pivot.plot(kind="bar", figsize=(12, 6))
plt.title("Drill-down: Purchases by Category, Visitor and Device")
plt.xlabel("Product Category")
plt.ylabel("Number of Purchases")
plt.xticks(rotation=30)
save_chart("Drilldown_Visualization.png")


# 3. SLICE: Mobile Device Only
df = pd.read_csv(os.path.join(folder, "Slice_Result.csv"))

plt.figure(figsize=(9, 5))
plt.bar(df["Dimension"], df["Purchases"])
plt.title("Slice: Mobile Device Shopping Behaviour")
plt.xlabel("Category and Visitor Type")
plt.ylabel("Number of Purchases")
plt.xticks(rotation=45, ha="right")
save_chart("Slice_Visualization.png")


# 4. DICE: Beauty and Clothing on Mobile and Tablet
df = pd.read_csv(os.path.join(folder, "Dice_Result.csv"))

parts = df["Dimension"].str.split(" | ", regex=False, expand=True)
df["Category"] = parts[0]
df["Visitor"] = parts[1]
df["Device"] = parts[2]

heatmap_data = df.pivot_table(
    index="Category",
    columns="Device",
    values="Purchases",
    aggfunc="sum",
    fill_value=0
)

plt.figure(figsize=(8, 5))
plt.imshow(heatmap_data, aspect="auto")
plt.colorbar(label="Number of Purchases")
plt.xticks(range(len(heatmap_data.columns)), heatmap_data.columns)
plt.yticks(range(len(heatmap_data.index)), heatmap_data.index)
plt.title("Dice: Beauty and Clothing by Device")

for i in range(len(heatmap_data.index)):
    for j in range(len(heatmap_data.columns)):
        plt.text(j, i, heatmap_data.iloc[i, j],
                 ha="center", va="center")

save_chart("Dice_Visualization.png")


# 5. PIVOT: Category vs Device
df = pd.read_csv(os.path.join(folder, "Pivot_Result.csv"))

categories = df["Product_Category"]
devices = ["Desktop", "Mobile", "Tablet"]

x = range(len(categories))
width = 0.25

plt.figure(figsize=(11, 6))

for i, device in enumerate(devices):
    plt.bar(
        [v + i * width for v in x],
        df[device + "_Purchases"],
        width,
        label=device
    )

plt.title("Pivot: Purchases by Product Category and Device")
plt.xlabel("Product Category")
plt.ylabel("Number of Purchases")
plt.xticks(
    [v + width for v in x],
    categories,
    rotation=30
)
plt.legend()
save_chart("Pivot_Visualization.png")

print("\nAll five data cube visualizations generated successfully!")
