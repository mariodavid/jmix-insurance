import os
import xml.etree.ElementTree as ET

def main():
    xml_path = "build/reports/jacoco/rootReport/jacoco.xml"
    summary_file = os.environ.get('GITHUB_STEP_SUMMARY')

    if not os.path.exists(xml_path):
        print(f"Coverage file not found: {xml_path}")
        return

    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()
        
        # The last counter elements are the totals for the entire project
        counters = root.findall('counter')
        
        stats = {}
        for c in counters:
            t = c.get('type')
            missed = int(c.get('missed', 0))
            covered = int(c.get('covered', 0))
            total = missed + covered
            pct = (covered / total * 100) if total > 0 else 0
            stats[t] = {"covered": covered, "missed": missed, "total": total, "pct": pct}

        line_pct = stats.get('LINE', {}).get('pct', 0)
        branch_pct = stats.get('BRANCH', {}).get('pct', 0)
        inst_pct = stats.get('INSTRUCTION', {}).get('pct', 0)

        line_passed = line_pct >= 75.0
        branch_passed = branch_pct >= 60.0

        summary = []
        summary.append("## 📊 Code Coverage Summary")
        summary.append("")
        summary.append("| Metric | Covered | Total | Coverage % | Threshold | Status |")
        summary.append("| --- | --- | --- | --- | --- | --- |")
        summary.append(f"| **Line Coverage** | {stats.get('LINE', {}).get('covered')} | {stats.get('LINE', {}).get('total')} | {line_pct:.2f}% | 75.00% | {'✅ PASSED' if line_passed else '❌ FAILED'} |")
        summary.append(f"| **Branch Coverage** | {stats.get('BRANCH', {}).get('covered')} | {stats.get('BRANCH', {}).get('total')} | {branch_pct:.2f}% | 60.00% | {'✅ PASSED' if branch_passed else '❌ FAILED'} |")
        summary.append(f"| **Instruction Coverage** | {stats.get('INSTRUCTION', {}).get('covered')} | {stats.get('INSTRUCTION', {}).get('total')} | {inst_pct:.2f}% | N/A | info |")
        summary.append("")
        
        if not (line_passed and branch_passed):
            summary.append("> [!CAUTION]")
            summary.append("> **Code Coverage verification failed!** Minimum thresholds are not met.")
        else:
            summary.append("> [!NOTE]")
            summary.append("> **Code Coverage verification passed!** All thresholds are met.")

        summary_content = "\n".join(summary)
        
        if summary_file:
            with open(summary_file, 'a') as f:
                f.write(summary_content + "\n")
        else:
            print(summary_content)
            
    except Exception as e:
        print(f"Error parsing coverage report: {e}")

if __name__ == "__main__":
    main()
