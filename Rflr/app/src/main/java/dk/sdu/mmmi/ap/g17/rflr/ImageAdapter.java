package dk.sdu.mmmi.ap.g17.rflr;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Morten on 03-05-2017.
 */

public class ImageAdapter extends BaseAdapter {

    private ArrayList<Integer> imagesToShow;
    private Context mContext;

    public ImageAdapter(Context con){
        mContext = con;
        imagesToShow = new ArrayList<>();
        addImages();
    }

    private void addImages(){
        imagesToShow.add(R.mipmap.die1);
        imagesToShow.add(R.mipmap.die2);
        imagesToShow.add(R.mipmap.die3);
        imagesToShow.add(R.mipmap.die4);
        imagesToShow.add(R.mipmap.die5);
        imagesToShow.add(R.mipmap.die6);
    }

    /**
     * Sets images to show in the view.
     * This method must be called each time your cup changes to show the correct images
     * @param cup holding dice for image representation
     */
    public void setDiceImages(Cup cup){
        imagesToShow.clear();
        for (Die die: cup.getDice()){
            imagesToShow.add(getDieImage(die.getValue()));
        }
    }

    @Override
    public int getCount() {
        return imagesToShow.size();
    }

    @Override
    public Object getItem(int position) {
        return imagesToShow.get(getCount() % position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(imagesToShow.get(position));
        return imageView;
    }

    /**
     * Used for getting the image of an integer representation of the die eyes
     * @param eyes
     * @return
     */
    private int getDieImage(int eyes) {
        switch (eyes) {
            case 1: return R.mipmap.die1;
            case 2: return R.mipmap.die2;
            case 3: return R.mipmap.die3;
            case 4: return R.mipmap.die4;
            case 5: return R.mipmap.die5;
            default: return R.mipmap.die6;
        }
    }
}
