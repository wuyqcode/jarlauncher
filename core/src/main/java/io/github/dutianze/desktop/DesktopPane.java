package io.github.dutianze.desktop;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.dutianze.jar.JarItemDto;

import java.util.ArrayList;
import java.util.List;

public class DesktopPane extends Table {

    public interface IconClickListener {
        void onDoubleClick(JarItemDto item);
    }

    public DesktopPane(Skin skin) {
        super(skin);
    }

    public void setup(List<Program> programList, int rowCount) {
        this.clear();
        this.setBackground("wallpaper");
        this.top().left().padLeft(10);

        int total = programList.size();
        int columnCount = (int) Math.ceil((double) total / rowCount);

        List<Table> columns = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            Table col = new Table();
            col.top().padRight(15);
            columns.add(col);
            this.add(col).top();
        }
        this.row();

        for (int i = 0; i < total; i++) {
            Program item = programList.get(i);
            int colIndex = i / rowCount;
            columns.get(colIndex).add(item.getDesktopIcon()).row();
        }
    }
}
